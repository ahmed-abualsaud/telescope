
package org.telescope.javel.framework.service.geolocation;

public class GoogleGeolocationProvider extends UniversalGeolocationProvider {

    private static final String URL = "https://www.googleapis.com/geolocation/v1/geolocate";

    public GoogleGeolocationProvider(String key) {
        super(URL, key);
    }

}
