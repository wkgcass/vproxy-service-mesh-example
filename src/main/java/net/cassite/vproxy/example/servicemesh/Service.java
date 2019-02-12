package net.cassite.vproxy.example.servicemesh;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.RedisClient;
import io.vertx.redis.RedisOptions;

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

        // redis
        RedisClient redisClient = RedisClient.create(vertx,
            new RedisOptions()
                .setAddress("127.0.0.1")
                .setPort(16379)
                .setAuth("mypassw0rd"));

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
                return;
            }
            redisClient.sadd("service", service + ".my.service.com:80:" + listenPort, r2 -> {
                if (r2.failed()) {
                    System.out.println("register service failed, err = " + r2.cause());
                    return;
                }
                if (r2.result() == 1) {
                    System.out.println("register service succeeded");
                } else {
                    System.out.println("register service failed, result = " + r2.result());
                }
            });
        });
    }

    private static void handleRouter(Router router) {
        router.get("/").handler(Service::handle);
    }

    private static void handle(RoutingContext rctx) {
        rctx.response().end("{\"service\":\"" + service + "\",\"resp\":\"" + respString + "\"}\r\n");
    }
}
