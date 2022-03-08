
package org.telescope.javel.framework.service.geolocation;

import org.telescope.model.CellTower;
import org.telescope.model.Network;
import org.telescope.server.Context;

import javax.json.JsonObject;
import javax.ws.rs.client.InvocationCallback;

public class OpenCellIdGeolocationProvider implements GeolocationProvider {

    private String url;

    public OpenCellIdGeolocationProvider(String url, String key) {
        if (url == null) {
            url = "http://opencellid.org/cell/get";
        }
        this.url = url + "?format=json&mcc=%d&mnc=%d&lac=%d&cellid=%d&key=" + key;
    }

    @Override
    public void getLocation(Network network, final LocationProviderCallback callback) {
        if (network.getCellTowers() != null && !network.getCellTowers().isEmpty()) {

            CellTower cellTower = network.getCellTowers().iterator().next();
            String request = String.format(url, cellTower.getMobileCountryCode(), cellTower.getMobileNetworkCode(),
                    cellTower.getLocationAreaCode(), cellTower.getCellId());

            Context.getClient().target(request).request().async().get(new InvocationCallback<JsonObject>() {
                @Override
                public void completed(JsonObject json) {
                    if (json.containsKey("lat") && json.containsKey("lon")) {
                        callback.onSuccess(
                                json.getJsonNumber("lat").doubleValue(),
                                json.getJsonNumber("lon").doubleValue(), 0);
                    } else {
                        callback.onFailure(new GeolocationException("Coordinates are missing"));
                    }
                }

                @Override
                public void failed(Throwable throwable) {
                    callback.onFailure(throwable);
                }
            });

        } else {
            callback.onFailure(new GeolocationException("No network information"));
        }
    }

}
