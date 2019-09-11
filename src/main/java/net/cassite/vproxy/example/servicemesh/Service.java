package net.cassite.vproxy.example.servicemesh;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import vclient.ServiceRegisterClient;

import java.lang.management.ManagementFactory;

public class Service {
    private static String service;
    private static String host;

    public static void main(String[] args) {
        if (args.length < 1) {
            throw new IllegalArgumentException();
        }
        service = args[0];
        host = ManagementFactory.getRuntimeMXBean().getName().split("@")[1];

        Vertx vertx = Vertx.vertx();

        // vproxy client
        ServiceRegisterClient vproxyClient = new ServiceRegisterClient(vertx,
            "my-service-" + service,
            "example",
            18080);

        // router
        Router router = Router.router(vertx);
        handleRouter(router);

        // http server
        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router);
        vproxyClient.register("eth0").setHandler(r -> {
            if (r.failed()) {
                System.out.println("register service " + service + " failed: " + r.cause().getMessage());
                System.exit(0);
                return;
            }
            System.out.println("register service " + service + " succeeded");
            int port = r.result();
            httpServer.listen(port, r2 -> {
                if (r2.failed()) {
                    // listen failed, which should not happen
                    System.out.println("listen on " + port + " failed: " + r2.cause());
                    System.exit(0);
                    return;
                }
                System.out.println("listen on " + port + " succeeded");
            });
        });

        // deregister before exiting
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("program is going to exit, try to deregister and shutdown the server");
            boolean[] wait = {true};
            vproxyClient.deregister().setHandler(r -> {
                if (r.failed()) {
                    System.err.println("deregister service " + service + " failed: " + r.cause().getMessage());
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
        rctx.response().end("{" +
            "\"service\":\"" + service + "\"," +
            "\"host\":\"" + host + "\"," +
            "\"port\":" + rctx.request().localAddress().port() +
            "}\r\n");
    }
}
