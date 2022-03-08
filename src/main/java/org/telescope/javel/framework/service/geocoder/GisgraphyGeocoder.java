
package org.telescope.javel.framework.service.geocoder;

import javax.json.JsonObject;

public class GisgraphyGeocoder extends JsonGeocoder {

    private static String formatUrl(String url) {
        if (url == null) {
            url = "http://services.gisgraphy.com/reversegeocoding/search";
        }
        url += "?format=json&lat=%f&lng=%f&from=1&to=1";
        return url;
    }

    public GisgraphyGeocoder(String url, int cacheSize, AddressFormat addressFormat) {
        super(formatUrl(url), cacheSize, addressFormat);
    }

    @Override
    public Address parseAddress(JsonObject json) {
        Address address = new Address();

        JsonObject result = json.getJsonArray("result").getJsonObject(0);

        if (result.containsKey("streetName")) {
            address.setStreet(result.getString("streetName"));
        }
        if (result.containsKey("city")) {
            address.setSettlement(result.getString("city"));
        }
        if (result.containsKey("state")) {
            address.setState(result.getString("state"));
        }
        if (result.containsKey("countryCode")) {
            address.setCountry(result.getString("countryCode"));
        }
        if (result.containsKey("formatedFull")) {
            address.setFormattedAddress(result.getString("formatedFull"));
        }

        return address;
    }

}
