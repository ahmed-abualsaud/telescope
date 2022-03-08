package org.telescope.server.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.config.Keys;
import org.telescope.javel.framework.service.geocoder.AddressFormat;
import org.telescope.javel.framework.service.geocoder.BanGeocoder;
import org.telescope.javel.framework.service.geocoder.BingMapsGeocoder;
import org.telescope.javel.framework.service.geocoder.FactualGeocoder;
import org.telescope.javel.framework.service.geocoder.GeocodeFarmGeocoder;
import org.telescope.javel.framework.service.geocoder.GeocodeXyzGeocoder;
import org.telescope.javel.framework.service.geocoder.Geocoder;
import org.telescope.javel.framework.service.geocoder.GisgraphyGeocoder;
import org.telescope.javel.framework.service.geocoder.GoogleGeocoder;
import org.telescope.javel.framework.service.geocoder.HereGeocoder;
import org.telescope.javel.framework.service.geocoder.MapQuestGeocoder;
import org.telescope.javel.framework.service.geocoder.MapTilerGeocoder;
import org.telescope.javel.framework.service.geocoder.MapboxGeocoder;
import org.telescope.javel.framework.service.geocoder.MapmyIndiaGeocoder;
import org.telescope.javel.framework.service.geocoder.NominatimGeocoder;
import org.telescope.javel.framework.service.geocoder.OpenCageGeocoder;
import org.telescope.javel.framework.service.geocoder.PositionStackGeocoder;
import org.telescope.javel.framework.service.geocoder.TomTomGeocoder;
import org.telescope.config.Config;
import org.telescope.server.Context;

import java.util.Map;

public class GeocoderHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeocoderHandler.class);

    public static void handle(
        Map<String, Object> state, 
        Map<String, Object> newPosition
    ) {
        if (!state.containsKey("address") || state.get("address") == null) {
            Geocoder geocoder;
            Config config = Context.getConfig();
            if (config.getBoolean(Keys.GEOCODER_ENABLE)) {
                String type = config.getString(Keys.GEOCODER_TYPE, "google");
                String url = config.getString(Keys.GEOCODER_URL);
                String id = config.getString(Keys.GEOCODER_ID);
                String key = config.getString(Keys.GEOCODER_KEY);
                String language = config.getString(Keys.GEOCODER_LANGUAGE);
                String formatString = config.getString(Keys.GEOCODER_FORMAT);
                AddressFormat addressFormat = formatString != null ? new AddressFormat(formatString) : new AddressFormat();
                int cacheSize = config.getInteger(Keys.GEOCODER_CACHE_SIZE);

                switch (type) {
                    case "nominatim":
                        geocoder = new NominatimGeocoder(url, key, language, cacheSize, addressFormat);
                    case "gisgraphy":
                        geocoder = new GisgraphyGeocoder(url, cacheSize, addressFormat);
                    case "mapquest":
                        geocoder = new MapQuestGeocoder(url, key, cacheSize, addressFormat);
                    case "opencage":
                        geocoder = new OpenCageGeocoder(url, key, cacheSize, addressFormat);
                    case "bingmaps":
                        geocoder = new BingMapsGeocoder(url, key, cacheSize, addressFormat);
                    case "factual":
                        geocoder = new FactualGeocoder(url, key, cacheSize, addressFormat);
                    case "geocodefarm":
                        geocoder = new GeocodeFarmGeocoder(key, language, cacheSize, addressFormat);
                    case "geocodexyz":
                        geocoder = new GeocodeXyzGeocoder(key, cacheSize, addressFormat);
                    case "ban":
                        geocoder = new BanGeocoder(cacheSize, addressFormat);
                    case "here":
                        geocoder = new HereGeocoder(url, id, key, language, cacheSize, addressFormat);
                    case "mapmyindia":
                        geocoder = new MapmyIndiaGeocoder(url, key, cacheSize, addressFormat);
                    case "tomtom":
                        geocoder = new TomTomGeocoder(url, key, cacheSize, addressFormat);
                    case "positionstack":
                        geocoder = new PositionStackGeocoder(key, cacheSize, addressFormat);
                    case "mapbox":
                        geocoder = new MapboxGeocoder(key, cacheSize, addressFormat);
                    case "maptiler":
                        geocoder = new MapTilerGeocoder(key, cacheSize, addressFormat);
                    default:
                        geocoder = new GoogleGeocoder(key, language, cacheSize, addressFormat);
                }

                geocoder.getAddress(((Number) state.get("latitude")).doubleValue(), 
                                    ((Number) state.get("longitude")).doubleValue(),
                    new Geocoder.ReverseGeocoderCallback() {
                        @Override
                        public void onSuccess(String address) {
                            newPosition.put("address", address);
                        }

                        @Override
                        public void onFailure(Throwable e) {
                            newPosition.put("address", null);
                            LOGGER.warn("Geocoding failed", e);
                        }
                    }
                );
            }
        }
    }
}
