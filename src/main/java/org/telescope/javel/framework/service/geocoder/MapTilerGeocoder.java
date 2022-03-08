
package org.telescope.javel.framework.service.geocoder;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class MapTilerGeocoder extends JsonGeocoder {

    public MapTilerGeocoder(String key, int cacheSize, AddressFormat addressFormat) {
        super("https://api.maptiler.com/geocoding/%2$f,%1$f.json?key=" + key, cacheSize, addressFormat);
    }

    @Override
    public Address parseAddress(JsonObject json) {
        JsonArray features = json.getJsonArray("features");

        if (!features.isEmpty()) {
            Address address = new Address();

            for (int i = 0; i < features.size(); i++) {
                JsonObject feature = features.getJsonObject(i);
                String type = feature.getJsonArray("place_type").getString(0);
                String value = feature.getString("text");
                switch (type) {
                    case "street":
                        address.setStreet(value);
                        break;
                    case "city":
                        address.setSettlement(value);
                        break;
                    case "county":
                        address.setDistrict(value);
                        break;
                    case "state":
                        address.setState(value);
                        break;
                    case "country":
                        address.setCountry(value);
                        break;
                    default:
                        break;
                }
                if (address.getFormattedAddress() == null) {
                    address.setFormattedAddress(feature.getString("place_name"));
                }
            }

            return address;
        }

        return null;
    }

    @Override
    protected String parseError(JsonObject json) {
        return null;
    }

}
