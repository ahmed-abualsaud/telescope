
package org.telescope.javel.framework.service.geocoder;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class OpenCageGeocoder extends JsonGeocoder {

    private static String formatUrl(String url, String key) {
        if (url == null) {
            url = "https://api.opencagedata.com/geocode/v1";
        }
        url += "/json?q=%f,%f&no_annotations=1&key=" + key;
        return url;
    }

    public OpenCageGeocoder(String url, String key, int cacheSize, AddressFormat addressFormat) {
        super(formatUrl(url, key), cacheSize, addressFormat);
    }

    @Override
    public Address parseAddress(JsonObject json) {
        JsonArray result = json.getJsonArray("results");
        if (result != null) {
            JsonObject location = result.getJsonObject(0).getJsonObject("components");
            if (location != null) {
                Address address = new Address();

                if (result.getJsonObject(0).containsKey("formatted")) {
                    address.setFormattedAddress(result.getJsonObject(0).getString("formatted"));
                }
                if (location.containsKey("building")) {
                    address.setHouse(location.getString("building"));
                }
                if (location.containsKey("house_number")) {
                    address.setHouse(location.getString("house_number"));
                }
                if (location.containsKey("road")) {
                    address.setStreet(location.getString("road"));
                }
                if (location.containsKey("suburb")) {
                    address.setSuburb(location.getString("suburb"));
                }
                if (location.containsKey("city")) {
                    address.setSettlement(location.getString("city"));
                }
                if (location.containsKey("city_district")) {
                    address.setSettlement(location.getString("city_district"));
                }
                if (location.containsKey("county")) {
                    address.setDistrict(location.getString("county"));
                }
                if (location.containsKey("state")) {
                    address.setState(location.getString("state"));
                }
                if (location.containsKey("country_code")) {
                    address.setCountry(location.getString("country_code").toUpperCase());
                }
                if (location.containsKey("postcode")) {
                    address.setPostcode(location.getString("postcode"));
                }

                return address;
            }
        }
        return null;
    }

}
