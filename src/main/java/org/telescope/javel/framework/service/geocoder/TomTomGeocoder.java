
package org.telescope.javel.framework.service.geocoder;

import javax.json.JsonArray;
import javax.json.JsonObject;

public class TomTomGeocoder extends JsonGeocoder {

    private static String formatUrl(String url, String key) {
        if (url == null) {
            url = "https://api.tomtom.com/search/2/reverseGeocode/";
        }
        url += "%f,%f.json?key=" + key;
        return url;
    }

    public TomTomGeocoder(String url, String key, int cacheSize, AddressFormat addressFormat) {
        super(formatUrl(url, key), cacheSize, addressFormat);
    }

    @Override
    public Address parseAddress(JsonObject json) {
        JsonArray addresses = json.getJsonArray("addresses");
        if (addresses != null) {
            JsonObject record = addresses.getJsonObject(0);
            if (record != null) {
                JsonObject location = record.getJsonObject("address");

                Address address = new Address();

                if (location.containsKey("streetNumber")) {
                    address.setHouse(location.getString("streetNumber"));
                }
                if (location.containsKey("street")) {
                    address.setStreet(location.getString("street"));
                }
                if (location.containsKey("municipality")) {
                    address.setSettlement(location.getString("municipality"));
                }
                if (location.containsKey("municipalitySubdivision")) {
                    address.setDistrict(location.getString("municipalitySubdivision"));
                }
                if (location.containsKey("countrySubdivision")) {
                    address.setState(location.getString("countrySubdivision"));
                }
                if (location.containsKey("country")) {
                    address.setCountry(location.getString("country").toUpperCase());
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
