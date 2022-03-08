
package org.telescope.javel.framework.service.geocoder;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class MapQuestGeocoder extends JsonGeocoder {

    private static String formatUrl(String url, String key) {
        if (url == null) {
            url = "http://www.mapquestapi.com/geocoding/v1/reverse";
        }
        url += "?key=" + key + "&location=%f,%f";
        return url;
    }

    public MapQuestGeocoder(String url, String key, int cacheSize, AddressFormat addressFormat) {
        super(formatUrl(url, key), cacheSize, addressFormat);
    }

    @Override
    public Address parseAddress(JsonObject json) {
        JsonArray result = json.getJsonArray("results");
        if (result != null) {
            JsonArray locations = result.getJsonObject(0).getJsonArray("locations");
            if (locations != null) {
                JsonObject location = locations.getJsonObject(0);

                Address address = new Address();

                if (location.containsKey("street")) {
                    address.setStreet(location.getString("street"));
                }
                if (location.containsKey("adminArea5")) {
                    address.setSettlement(location.getString("adminArea5"));
                }
                if (location.containsKey("adminArea4")) {
                    address.setDistrict(location.getString("adminArea4"));
                }
                if (location.containsKey("adminArea3")) {
                    address.setState(location.getString("adminArea3"));
                }
                if (location.containsKey("adminArea1")) {
                    address.setCountry(location.getString("adminArea1").toUpperCase());
                }
                if (location.containsKey("postalCode")) {
                    address.setPostcode(location.getString("postalCode"));
                }

                return address;
            }
        }
        return null;
    }

}
