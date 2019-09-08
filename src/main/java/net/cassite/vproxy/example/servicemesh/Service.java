package net.cassite.vproxy.example.servicemesh;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;

import java.lang.management.ManagementFactory;

public class Service {
    private static final int listenPort = 8080;
    private static String service;
    private static String respString;

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException();
        }
        service = args[0];
        respString = ManagementFactory.getRuntimeMXBean().getName().split("@")[1];

        Vertx vertx = Vertx.vertx();

        // vproxy client
        WebClient vproxyClient = WebClient.create(vertx);

        // router
        Router router = Router.router(vertx);
        handleRouter(router);

        // http server
        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router);
        httpServer.listen(listenPort, r -> {
            if (r.failed()) {
                // listen failed, which should not happen
                System.out.println("listen on " + listenPort + " failed");
                System.exit(1);
                return;
            }
            // register when listen succeeded
            vproxyClient.post(18080, "127.0.0.1", "/api/v1/module/smart-service-delegate")
                .sendJsonObject(new JsonObject()
                        .put("name", "delegate-service-" + service)
                        .put("service", "my-service-" + service)
                        .put("zone", "example")
                        .put("nic", "eth0")
                        .put("exposedPort", listenPort)
                        .put("ipType", "v4") // this field is optional
                    ,
                    r1 -> {
                        if (r1.failed() || r1.result().statusCode() != 204) {
                            System.err.println("register service " + service + " failed");
                            System.exit(1);
                            return;
                        }
                        System.out.println("register service " + service + " succeeded");
                    });
        });

        // deregister before exiting
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("program is going to exit, try to deregister and shutdown the server");
            boolean[] wait = {true};
            vproxyClient.delete(18080, "127.0.0.1", "/api/v1/module/smart-service-delegate/delegate-service-" + service)
                .send(r -> {
                    if (r.failed() || r.result().statusCode() != 204) {
                        System.err.println("deregister service " + service + " failed");
                    } else {
                        System.out.println("deregister service " + service + " succeeded");
                    }
                    // wait for 2 seconds before actually shutting down the service
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        // ignore
                    }
                    wait[0] = false;
                });
            while (wait[0]) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }));
    }

    private static void handleRouter(Router router) {
        router.get("/").handler(Service::handle);
    }

    private static void handle(RoutingContext rctx) {
        rctx.response().end("{\"service\":\"" + service + "\",\"resp\":\"" + respString + "\"}\r\n");
    }
}
