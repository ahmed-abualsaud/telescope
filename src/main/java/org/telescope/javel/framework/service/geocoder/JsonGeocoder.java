package org.telescope.javel.framework.service.geocoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.database.StatisticsManager;
import org.telescope.server.Context;
import org.telescope.server.Main;

import javax.json.JsonObject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.InvocationCallback;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class JsonGeocoder implements Geocoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonGeocoder.class);

    private final String url;
    private final AddressFormat addressFormat;

    private Map<Map.Entry<Double, Double>, String> cache;

    public JsonGeocoder(String url, final int cacheSize, AddressFormat addressFormat) {
        this.url = url;
        this.addressFormat = addressFormat;
        if (cacheSize > 0) {
            this.cache = Collections.synchronizedMap(new LinkedHashMap<Map.Entry<Double, Double>, String>() {
                @Override
                protected boolean removeEldestEntry(Map.Entry eldest) {
                    return size() > cacheSize;
                }
            });
        }
    }

    protected String readValue(JsonObject object, String key) {
        if (object.containsKey(key) && !object.isNull(key)) {
            return object.getString(key);
        }
        return null;
    }

    private String handleResponse(
            double latitude, double longitude, JsonObject json, ReverseGeocoderCallback callback) {

        Address address = parseAddress(json);
        if (address != null) {
            String formattedAddress = addressFormat.format(address);
            if (cache != null) {
                cache.put(new AbstractMap.SimpleImmutableEntry<>(latitude, longitude), formattedAddress);
            }
            if (callback != null) {
                callback.onSuccess(formattedAddress);
            }
            return formattedAddress;
        } else {
            String msg = "Empty address. Error: " + parseError(json);
            if (callback != null) {
                callback.onFailure(new GeocoderException(msg));
            } else {
                LOGGER.warn(msg);
            }
        }
        return null;
    }

    @Override
    public String getAddress(
            final double latitude, final double longitude, final ReverseGeocoderCallback callback) {

        if (cache != null) {
            String cachedAddress = cache.get(new AbstractMap.SimpleImmutableEntry<>(latitude, longitude));
            if (cachedAddress != null) {
                if (callback != null) {
                    callback.onSuccess(cachedAddress);
                }
                return cachedAddress;
            }
        }

        if (Main.getInjector() != null) {
            Main.getInjector().getInstance(StatisticsManager.class).registerGeocoderRequest();
        }

        Invocation.Builder request = Context.getClient().target(String.format(url, latitude, longitude)).request();

        if (callback != null) {
            request.async().get(new InvocationCallback<JsonObject>() {
                @Override
                public void completed(JsonObject json) {
                    handleResponse(latitude, longitude, json, callback);
                }

                @Override
                public void failed(Throwable throwable) {
                    callback.onFailure(throwable);
                }
            });
        } else {
            try {
                return handleResponse(latitude, longitude, request.get(JsonObject.class), null);
            } catch (WebApplicationException e) {
                LOGGER.warn("Geocoder network error", e);
            }
        }
        return null;
    }

    public abstract Address parseAddress(JsonObject json);

    protected String parseError(JsonObject json) {
        return null;
    }

}
