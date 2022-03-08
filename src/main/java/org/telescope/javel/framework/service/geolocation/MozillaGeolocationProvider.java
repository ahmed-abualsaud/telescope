
package org.telescope.javel.framework.service.geolocation;

public class MozillaGeolocationProvider extends UniversalGeolocationProvider {

    private static final String URL = "https://location.services.mozilla.com/v1/geolocate";

    public MozillaGeolocationProvider(String key) {
        super(URL, key != null ? key : "test");
    }

}
