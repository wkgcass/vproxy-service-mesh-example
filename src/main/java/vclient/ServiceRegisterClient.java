/*
 * The MIT License
 *
 * Copyright 2019 wkgcass
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package vclient;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import java.net.*;
import java.util.Enumeration;

/**
 * This client is for registering/deregistering a service into/from the vproxy sidecar.<br>
 * This class depends on eclipse vert.x webclient 3.x.
 * You may copy past this java file to your project, add webclient dependency, and begin to use.
 * <br>
 * This lib is not using vproxy vclient lib because vproxy lib is not yet uploaded to maven central,
 * so vproxy vclient lib require you to copy-paste all vproxy packages into your project,
 * which you might not need. And vproxy uses the most modern JDK, which you may not be using for now.
 */
@SuppressWarnings({"WeakerAccess", "unused", "DuplicatedCode"})
public class ServiceRegisterClient {
    private final String alias;
    private final String service;
    private final String zone;
    private final WebClient httpClient;

    /**
     * Construct the client with the port to vproxy sidecar.<br>
     * No need to set l3addr because sidecar should always run on localhost.
     *
     * @param vertx   the vproxy instance
     * @param service service name.
     * @param zone    zone name, or data center name.
     * @param port    the port to vproxy sidecar.
     */
    public ServiceRegisterClient(Vertx vertx, String service, String zone, int port) {
        this(vertx, service + "_" + zone, service, zone, port);
    }

    /**
     * Construct the client with the port to vproxy sidecar.<br>
     * No need to set l3addr because sidecar should always run on localhost.
     *
     * @param vertx   the vproxy instance
     * @param alias   specify alias of the smart-service-delegate of vproxy
     * @param service service name.
     * @param zone    zone name, or data center name.
     * @param port    the port to vproxy sidecar.
     */
    public ServiceRegisterClient(Vertx vertx, String alias, String service, String zone, int port) {
        this.alias = alias;
        this.service = service;
        this.zone = zone;
        this.httpClient = WebClient.create(vertx, new WebClientOptions()
            .setDefaultHost("127.0.0.1")
            .setDefaultPort(port));
    }

    public interface NicChooser {
        boolean choose(NetworkInterface nic, InetAddress address);
    }

    /**
     * Register the service to sidecar and let sidecar allocate a port for this service to listen.
     *
     * @param nicChooser a callback function helps you choose the nic and ip version.
     * @return the port number you should listen.
     * @throws RuntimeException any error happens here.
     */
    public Future<Integer> register(NicChooser nicChooser) throws RuntimeException {
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                Enumeration<InetAddress> l3addrs = nic.getInetAddresses();
                while (l3addrs.hasMoreElements()) {
                    InetAddress address = l3addrs.nextElement();
                    if (nicChooser.choose(nic, address)) {
                        return register0(nic.getName(), address instanceof Inet6Address, 0);
                    }
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        throw new RuntimeException("User code did not choose an nic");
    }

    /**
     * Register the service to sidecar.
     *
     * @param nicChooser a callback function helps you choose the nic and ip version.
     * @param port       listening port of the service.
     * @return future object.
     * @throws RuntimeException any error happens here.
     */
    public Future<Void> register(NicChooser nicChooser, int port) throws RuntimeException {
        try {
            Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
            while (nics.hasMoreElements()) {
                NetworkInterface nic = nics.nextElement();
                Enumeration<InetAddress> l3addrs = nic.getInetAddresses();
                while (l3addrs.hasMoreElements()) {
                    InetAddress address = l3addrs.nextElement();
                    if (nicChooser.choose(nic, address)) {
                        return register0(nic.getName(), address instanceof Inet6Address, port).mapEmpty();
                    }
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        throw new RuntimeException("User code did not choose an nic");
    }

    /**
     * Register the service to sidecar with ip-type=v4 and let sidecar allocate a port for this service to listen.
     *
     * @param nic nic name.
     * @return the port number you should listen.
     * @throws RuntimeException any error happens here.
     */
    public Future<Integer> register(String nic) throws RuntimeException {
        return register(nic, false);
    }

    /**
     * Register the service to sidecar with ip-type=v4.
     *
     * @param nic  nic name.
     * @param port listening port of the service.
     * @return future object.
     * @throws RuntimeException any error happens here.
     */
    public Future<Void> register(String nic, int port) throws RuntimeException {
        return register(nic, false, port).mapEmpty();
    }

    /**
     * Register the service to sidecar and let sidecar allocate a port for this service to listen.
     *
     * @param nic     nic name.
     * @param useIpv6 whether to use ipv6, true: use ipv6, false: use ipv4.
     * @return the port number you should listen.
     * @throws RuntimeException any error happens here.
     */
    public Future<Integer> register(String nic, boolean useIpv6) throws RuntimeException {
        try {
            return register0(nic, useIpv6, 0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Register the service to sidecar and let sidecar allocate a port for this service to listen.
     *
     * @param nic     nic name.
     * @param useIpv6 whether to use ipv6, true: use ipv6, false: use ipv4.
     * @param port    listening port of the service.
     * @return future object.
     * @throws RuntimeException any error happens here.
     */
    public Future<Void> register(String nic, boolean useIpv6, int port) throws RuntimeException {
        try {
            return register0(nic, useIpv6, port).mapEmpty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Future<Void> deregister() {
        Future<Void> ret = Future.future();
        httpClient.delete("/api/v1/module/smart-service-delegate/" + alias).send(r -> {
            if (r.failed()) {
                ret.fail(r.cause());
                return;
            }
            HttpResponse resp = r.result();
            if (resp.statusCode() != 204) {
                ret.fail("response is not 204: " + resp.statusCode() + ", body: " + resp.bodyAsString());
                return;
            }
            ret.complete();
        });
        return ret;
    }

    private Future<Integer> register0(String nic, boolean useIpv6, int port) {
        return get().compose(o -> {
            if (o == null) {
                return create(nic, useIpv6, port);
            } else {
                return validate(o, port);
            }
        });
    }

    private Future<JsonObject> get() {
        Future<JsonObject> ret = Future.future();
        httpClient.get("/api/v1/module/smart-service-delegate/" + alias).send(r -> {
            if (r.failed()) {
                ret.fail(r.cause());
                return;
            }
            HttpResponse resp = r.result();
            if (resp.statusCode() == 404) {
                ret.complete(null);
                return;
            }
            if (resp.statusCode() != 200) {
                ret.fail("response is not 200: " + resp.statusCode() + ", body: " + resp.bodyAsString());
                return;
            }
            ret.complete(resp.bodyAsJsonObject());
        });
        return ret;
    }

    private Future<Integer> create(String nic, boolean useIpv6, int port) {
        return add(nic, useIpv6, port).compose(v -> get()).map(o -> o.getInteger("exposedPort"));
    }

    private Future<Void> add(String nic, boolean useIpv6, int port) {
        Future<Void> ret = Future.future();
        httpClient.post("/api/v1/module/smart-service-delegate").sendJsonObject(
            new JsonObject()
                .put("name", alias)
                .put("service", service)
                .put("zone", zone)
                .put("nic", nic)
                .put("ipType", useIpv6 ? "v6" : "v4")
                .put("exposedPort", port),
            r -> {
                if (r.failed()) {
                    ret.fail(r.cause());
                    return;
                }
                HttpResponse resp = r.result();
                if (resp.statusCode() != 204) {
                    ret.fail("response is not 204: " + resp.statusCode() + ", body: " + resp.bodyAsString());
                    return;
                }
                ret.complete();
            }
        );
        return ret;
    }

    private Future<Integer> validate(JsonObject o, int port) {
        String service = o.getString("service");
        String zone = o.getString("zone");
        if (!service.equals(this.service) || !zone.equals(this.zone)) {
            return Future.failedFuture("smart-service-delegate with alias " + alias + " have different service or zone: " + o);
        }
        String status = o.getString("status");
        if (status.equals("UP")) {
            return Future.failedFuture("smart-service-delegate with alias " + alias + " is currently UP: " + o);
        }
        if (port != 0 && o.getInteger("exposedPort") != port) {
            return Future.failedFuture("smart-service-delegate with alias " + alias + " have a different port: " + o);
        }
        return Future.succeededFuture(o.getInteger("exposedPort"));
    }
}
