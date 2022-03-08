
package org.telescope.javel.framework.service.geocoder;

/*
 * API documentation: https://adresse.data.gouv.fr/api
 */

import javax.json.JsonArray;
import javax.json.JsonObject;

public class BanGeocoder extends JsonGeocoder {

    public BanGeocoder(int cacheSize, AddressFormat addressFormat) {
        super("https://api-adresse.data.gouv.fr/reverse/?lat=%f&lon=%f", cacheSize, addressFormat);
    }

    @Override
    public Address parseAddress(JsonObject json) {
        JsonArray result = json.getJsonArray("features");

        if (result != null && !result.isEmpty()) {
            JsonObject location = result.getJsonObject(0).getJsonObject("properties");
            Address address = new Address();

            address.setCountry("FR");
            if (location.containsKey("postcode")) {
                address.setPostcode(location.getString("postcode"));
            }
            if (location.containsKey("context")) {
                address.setDistrict(location.getString("context"));
            }
            if (location.containsKey("name")) {
                address.setStreet(location.getString("name"));
            }
            if (location.containsKey("city")) {
                address.setSettlement(location.getString("city"));
            }
            if (location.containsKey("housenumber")) {
                address.setHouse(location.getString("housenumber"));
            }
            if (location.containsKey("label")) {
                address.setFormattedAddress(location.getString("label"));
            }

            return address;
        }

        return null;
    }

}
