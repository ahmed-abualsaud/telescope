package org.telescope.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import org.telescope.app.route.Guard;
import org.telescope.config.Config;
import org.telescope.config.Keys;
import org.telescope.database.AttributesManager;
import org.telescope.database.CalendarManager;
import org.telescope.database.DataManager;
import org.telescope.database.DeviceManager;
import org.telescope.database.GeofenceManager;
import org.telescope.database.IdentityManager;
import org.telescope.database.MaintenancesManager;
import org.telescope.database.StatisticsManager;
import org.telescope.handler.ComputedAttributesHandler;
import org.telescope.handler.CopyAttributesHandler;
import org.telescope.handler.DefaultDataHandler;
import org.telescope.handler.DistanceHandler;
import org.telescope.handler.EngineHoursHandler;
import org.telescope.handler.FilterHandler;
import org.telescope.handler.GeocoderHandler;
import org.telescope.handler.GeolocationHandler;
import org.telescope.handler.HemisphereHandler;
import org.telescope.handler.MotionHandler;
import org.telescope.handler.RemoteAddressHandler;
import org.telescope.handler.SpeedLimitHandler;
import org.telescope.handler.TimeHandler;
import org.telescope.handler.events.AlertEventHandler;
import org.telescope.handler.events.BehaviorEventHandler;
import org.telescope.handler.events.CommandResultEventHandler;
import org.telescope.handler.events.DriverEventHandler;
import org.telescope.handler.events.FuelDropEventHandler;
import org.telescope.handler.events.GeofenceEventHandler;
import org.telescope.handler.events.IgnitionEventHandler;
import org.telescope.handler.events.MaintenanceEventHandler;
import org.telescope.handler.events.MotionEventHandler;
import org.telescope.handler.events.OverspeedEventHandler;
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
import org.telescope.javel.framework.service.geolocation.GeolocationProvider;
import org.telescope.javel.framework.service.geolocation.GoogleGeolocationProvider;
import org.telescope.javel.framework.service.geolocation.MozillaGeolocationProvider;
import org.telescope.javel.framework.service.geolocation.OpenCellIdGeolocationProvider;
import org.telescope.javel.framework.service.geolocation.UnwiredGeolocationProvider;
import org.telescope.reports.model.TripsConfig;

import javax.annotation.Nullable;
import javax.ws.rs.client.Client;
import io.netty.util.Timer;
import org.telescope.speedlimit.OverpassSpeedLimitProvider;
import org.telescope.speedlimit.SpeedLimitProvider;

public class MainModule extends AbstractModule {

    @Provides
    public static ObjectMapper provideObjectMapper() {
        return Context.getObjectMapper();
    }

    @Provides
    public static Config provideConfig() {
        return Context.getConfig();
    }

    @Provides
    public static DataManager provideDataManager() {
        return Context.getDataManager();
    }

    @Provides
    public static IdentityManager provideIdentityManager() {
        return Context.getIdentityManager();
    }

    @Provides
    public static Client provideClient() {
        return Context.getClient();
    }

    @Provides
    public static TripsConfig provideTripsConfig() {
        return Context.getTripsConfig();
    }

    @Provides
    public static DeviceManager provideDeviceManager() {
        return Context.getDeviceManager();
    }

    @Provides
    public static GeofenceManager provideGeofenceManager() {
        return Context.getGeofenceManager();
    }

    @Provides
    public static CalendarManager provideCalendarManager() {
        return Context.getCalendarManager();
    }

    @Provides
    public static AttributesManager provideAttributesManager() {
        return Context.getAttributesManager();
    }

    @Provides
    public static MaintenancesManager provideMaintenancesManager() {
        return Context.getMaintenancesManager();
    }
    
    @Singleton
    @Provides
    public static Guard provideGuard() {
        return new Guard();
    }

    @Singleton
    @Provides
    public static StatisticsManager provideStatisticsManager(
            Config config, DataManager dataManager, Client client, ObjectMapper objectMapper) {
        return new StatisticsManager(config, dataManager, client, objectMapper);
    }

    @Singleton
    @Provides
    public static Geocoder provideGeocoder(Config config) {
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
                    return new NominatimGeocoder(url, key, language, cacheSize, addressFormat);
                case "gisgraphy":
                    return new GisgraphyGeocoder(url, cacheSize, addressFormat);
                case "mapquest":
                    return new MapQuestGeocoder(url, key, cacheSize, addressFormat);
                case "opencage":
                    return new OpenCageGeocoder(url, key, cacheSize, addressFormat);
                case "bingmaps":
                    return new BingMapsGeocoder(url, key, cacheSize, addressFormat);
                case "factual":
                    return new FactualGeocoder(url, key, cacheSize, addressFormat);
                case "geocodefarm":
                    return new GeocodeFarmGeocoder(key, language, cacheSize, addressFormat);
                case "geocodexyz":
                    return new GeocodeXyzGeocoder(key, cacheSize, addressFormat);
                case "ban":
                    return new BanGeocoder(cacheSize, addressFormat);
                case "here":
                    return new HereGeocoder(url, id, key, language, cacheSize, addressFormat);
                case "mapmyindia":
                    return new MapmyIndiaGeocoder(url, key, cacheSize, addressFormat);
                case "tomtom":
                    return new TomTomGeocoder(url, key, cacheSize, addressFormat);
                case "positionstack":
                    return new PositionStackGeocoder(key, cacheSize, addressFormat);
                case "mapbox":
                    return new MapboxGeocoder(key, cacheSize, addressFormat);
                case "maptiler":
                    return new MapTilerGeocoder(key, cacheSize, addressFormat);
                default:
                    return new GoogleGeocoder(key, language, cacheSize, addressFormat);
            }
        }
        return null;
    }

    @Singleton
    @Provides
    public static GeolocationProvider provideGeolocationProvider(Config config) {
        if (config.getBoolean(Keys.GEOLOCATION_ENABLE)) {
            String type = config.getString(Keys.GEOLOCATION_TYPE, "mozilla");
            String url = config.getString(Keys.GEOLOCATION_URL);
            String key = config.getString(Keys.GEOLOCATION_KEY);
            switch (type) {
                case "google":
                    return new GoogleGeolocationProvider(key);
                case "opencellid":
                    return new OpenCellIdGeolocationProvider(url, key);
                case "unwired":
                    return new UnwiredGeolocationProvider(url, key);
                default:
                    return new MozillaGeolocationProvider(key);
            }
        }
        return null;
    }

    @Singleton
    @Provides
    public static SpeedLimitProvider provideSpeedLimitProvider(Config config) {
        if (config.getBoolean(Keys.SPEED_LIMIT_ENABLE)) {
            String type = config.getString(Keys.SPEED_LIMIT_TYPE, "overpass");
            String url = config.getString(Keys.SPEED_LIMIT_URL);
            switch (type) {
                case "overpass":
                default:
                    return new OverpassSpeedLimitProvider(url);
            }
        }
        return null;
    }

    @Singleton
    @Provides
    public static DistanceHandler provideDistanceHandler(Config config, IdentityManager identityManager) {
        return new DistanceHandler(config, identityManager);
    }

    @Singleton
    @Provides
    public static FilterHandler provideFilterHandler(Config config) {
        if (config.getBoolean(Keys.FILTER_ENABLE)) {
            return new FilterHandler(config);
        }
        return null;
    }

    @Singleton
    @Provides
    public static HemisphereHandler provideHemisphereHandler(Config config) {
        if (config.hasKey(Keys.LOCATION_LATITUDE_HEMISPHERE) || config.hasKey(Keys.LOCATION_LONGITUDE_HEMISPHERE)) {
            return new HemisphereHandler(config);
        }
        return null;
    }

    @Singleton
    @Provides
    public static RemoteAddressHandler provideRemoteAddressHandler(Config config) {
        if (config.getBoolean(Keys.PROCESSING_REMOTE_ADDRESS_ENABLE)) {
            return new RemoteAddressHandler();
        }
        return null;
    }

    @Singleton
    @Provides
    public static WebDataHandler provideWebDataHandler(
            Config config, IdentityManager identityManager, ObjectMapper objectMapper, Client client) {
        if (config.hasKey(Keys.FORWARD_URL)) {
            return new WebDataHandler(config, identityManager, objectMapper, client);
        }
        return null;
    }

    @Singleton
    @Provides
    public static GeolocationHandler provideGeolocationHandler(
            Config config, @Nullable GeolocationProvider geolocationProvider, StatisticsManager statisticsManager) {
        if (geolocationProvider != null) {
            return new GeolocationHandler(config, geolocationProvider, statisticsManager);
        }
        return null;
    }

    @Singleton
    @Provides
    public static GeocoderHandler provideGeocoderHandler(
            Config config, @Nullable Geocoder geocoder, IdentityManager identityManager) {
        if (geocoder != null) {
            return new GeocoderHandler(config, geocoder, identityManager);
        }
        return null;
    }

    @Singleton
    @Provides
    public static SpeedLimitHandler provideSpeedLimitHandler(@Nullable SpeedLimitProvider speedLimitProvider) {
        if (speedLimitProvider != null) {
            return new SpeedLimitHandler(speedLimitProvider);
        }
        return null;
    }

    @Singleton
    @Provides
    public static MotionHandler provideMotionHandler(TripsConfig tripsConfig) {
        return new MotionHandler(tripsConfig.getSpeedThreshold());
    }

    @Singleton
    @Provides
    public static EngineHoursHandler provideEngineHoursHandler(Config config, IdentityManager identityManager) {
        if (config.getBoolean(Keys.PROCESSING_ENGINE_HOURS_ENABLE)) {
            return new EngineHoursHandler(identityManager);
        }
        return null;
    }

    @Singleton
    @Provides
    public static CopyAttributesHandler provideCopyAttributesHandler(Config config, IdentityManager identityManager) {
        if (config.getBoolean(Keys.PROCESSING_COPY_ATTRIBUTES_ENABLE)) {
            return new CopyAttributesHandler(identityManager);
        }
        return null;
    }

    @Singleton
    @Provides
    public static ComputedAttributesHandler provideComputedAttributesHandler(
            Config config, IdentityManager identityManager, AttributesManager attributesManager) {
        if (config.getBoolean(Keys.PROCESSING_COMPUTED_ATTRIBUTES_ENABLE)) {
            return new ComputedAttributesHandler(config, identityManager, attributesManager);
        }
        return null;
    }

    @Singleton
    @Provides
    public static TimeHandler provideTimeHandler(Config config) {
        if (config.hasKey(Keys.TIME_OVERRIDE)) {
            return new TimeHandler(config);
        }
        return null;
    }

    @Singleton
    @Provides
    public static DefaultDataHandler provideDefaultDataHandler(@Nullable DataManager dataManager) {
        if (dataManager != null) {
            return new DefaultDataHandler(dataManager);
        }
        return null;
    }

    @Singleton
    @Provides
    public static CommandResultEventHandler provideCommandResultEventHandler() {
        return new CommandResultEventHandler();
    }

    @Singleton
    @Provides
    public static OverspeedEventHandler provideOverspeedEventHandler(
            Config config, DeviceManager deviceManager, GeofenceManager geofenceManager) {
        return new OverspeedEventHandler(config, deviceManager, geofenceManager);
    }

    @Singleton
    @Provides
    public static BehaviorEventHandler provideBehaviorEventHandler(Config config, IdentityManager identityManager) {
        return new BehaviorEventHandler(config, identityManager);
    }

    @Singleton
    @Provides
    public static FuelDropEventHandler provideFuelDropEventHandler(IdentityManager identityManager) {
        return new FuelDropEventHandler(identityManager);
    }

    @Singleton
    @Provides
    public static MotionEventHandler provideMotionEventHandler(
            IdentityManager identityManager, DeviceManager deviceManager, TripsConfig tripsConfig) {
        return new MotionEventHandler(identityManager, deviceManager, tripsConfig);
    }

    @Singleton
    @Provides
    public static GeofenceEventHandler provideGeofenceEventHandler(
            IdentityManager identityManager, GeofenceManager geofenceManager, CalendarManager calendarManager) {
        return new GeofenceEventHandler(identityManager, geofenceManager, calendarManager);
    }

    @Singleton
    @Provides
    public static AlertEventHandler provideAlertEventHandler(Config config, IdentityManager identityManager) {
        return new AlertEventHandler(config, identityManager);
    }

    @Singleton
    @Provides
    public static IgnitionEventHandler provideIgnitionEventHandler(IdentityManager identityManager) {
        return new IgnitionEventHandler(identityManager);
    }

    @Singleton
    @Provides
    public static MaintenanceEventHandler provideMaintenanceEventHandler(
            IdentityManager identityManager, MaintenancesManager maintenancesManager) {
        return new MaintenanceEventHandler(identityManager, maintenancesManager);
    }

    @Singleton
    @Provides
    public static DriverEventHandler provideDriverEventHandler(IdentityManager identityManager) {
        return new DriverEventHandler(identityManager);
    }

    @Singleton
    @Provides
    public static Timer provideTimer() {
        return GlobalTimer.getTimer();
    }

    @Override
    protected void configure() {
        binder().requireExplicitBindings();
    }

}
