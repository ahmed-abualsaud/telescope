
package org.telescope.javel.framework.service.geolocation;

import org.telescope.model.Network;
import org.telescope.server.Context;

import javax.json.JsonObject;
import javax.ws.rs.client.AsyncInvoker;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;

public class UniversalGeolocationProvider implements GeolocationProvider {

    private String url;

    public UniversalGeolocationProvider(String url, String key) {
        this.url = url + "?key=" + key;
    }

    @Override
    public void getLocation(Network network, final LocationProviderCallback callback) {
        AsyncInvoker invoker = Context.getClient().target(url).request().async();
        invoker.post(Entity.json(network), new InvocationCallback<JsonObject>() {
            @Override
            public void completed(JsonObject json) {
                if (json.containsKey("error")) {
                    callback.onFailure(new GeolocationException(json.getJsonObject("error").getString("message")));
                } else {
                    JsonObject location = json.getJsonObject("location");
                    callback.onSuccess(
                            location.getJsonNumber("lat").doubleValue(),
                            location.getJsonNumber("lng").doubleValue(),
                            json.getJsonNumber("accuracy").doubleValue());
                }
            }

            @Override
            public void failed(Throwable throwable) {
                callback.onFailure(throwable);
            }
        });
    }

}
