package net.cassite.vproxy.example.servicemesh;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.net.ProxyOptions;
import io.vertx.core.net.ProxyType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class Frontend {
    private static final int listenPort = 8080;
    private static WebClient webClient;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();

        // webClient
        webClient = WebClient.create(vertx,
            new WebClientOptions().setProxyOptions(
                new ProxyOptions()
                    .setType(ProxyType.SOCKS5)
                    .setHost("127.0.0.1")
                    .setPort(1080)
            )
                .setKeepAlive(false) // disable keep-alive so we can get results on different backends
        );

        // router
        Router router = Router.router(vertx);
        handleRouter(router);

        // http server
        HttpServer httpServer = vertx.createHttpServer();
        httpServer.requestHandler(router);
        httpServer.listen(listenPort);
    }

    private static void handleRouter(Router router) {
        router.get("/service-a").handler(Frontend::a);
        router.get("/service-b").handler(Frontend::b);
    }

    private static void a(RoutingContext rctx) {
        webClient.get(80, "a.my.service.com", "/").send(r -> {
            if (r.failed()) {
                rctx.response().setStatusCode(500).end("request a.my.service.com failed: " + r.cause());
                return;
            }
            rctx.response().end(r.result().body());
        });
    }

    private static void b(RoutingContext rctx) {
        webClient.get(80, "b.my.service.com", "/").send(r -> {
            if (r.failed()) {
                rctx.response().setStatusCode(500).end("request b.my.service.com failed: " + r.cause());
                return;
            }
            rctx.response().end(r.result().body());
        });
    }
}
