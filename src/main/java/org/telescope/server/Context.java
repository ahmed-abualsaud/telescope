package org.telescope.server;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.velocity.app.VelocityEngine;
import org.eclipse.jetty.util.URIUtil;
import org.telescope.config.Config;
import org.telescope.config.Keys;
import org.telescope.database.QueryBuilder;
import org.telescope.database.AttributesManager;
import org.telescope.database.BaseObjectManager;
import org.telescope.database.CalendarManager;
import org.telescope.database.CommandsManager;
import org.telescope.database.ConnectionManager;
import org.telescope.database.DataManager;
import org.telescope.database.DeviceManager;
import org.telescope.database.DriversManager;
import org.telescope.database.GeofenceManager;
import org.telescope.database.GroupsManager;
import org.telescope.database.IdentityManager;
import org.telescope.database.LdapProvider;
import org.telescope.database.MailManager;
import org.telescope.database.MaintenancesManager;
import org.telescope.database.MediaManager;
import org.telescope.database.NotificationManager;
import org.telescope.database.OrderManager;
import org.telescope.database.PermissionsManager;
import org.telescope.database.UsersManager;
import org.telescope.javel.framework.event.Event;
import org.telescope.javel.framework.helper.Log;
import org.telescope.javel.framework.helper.SanitizerModule;
import org.telescope.javel.framework.job.ScheduleManager;
import org.telescope.javel.framework.model.ModelManager;
import org.telescope.javel.framework.notification.EventForwarder;
import org.telescope.javel.framework.notification.NotificatorManager;
import org.telescope.javel.framework.service.geocoder.Geocoder;
import org.telescope.javel.framework.service.sms.HttpSmsClient;
import org.telescope.javel.framework.service.sms.SmsManager;
import org.telescope.javel.framework.service.sms.SnsSmsClient;
import org.telescope.javel.framework.service.storage.AzureStorage;
import org.telescope.javel.framework.websocket.WebsocketManager;
import org.telescope.javel.framework.websocket.driver.*;
import org.telescope.model.Attribute;
import org.telescope.model.BaseModel;
import org.telescope.model.Calendar;
import org.telescope.model.Command;
import org.telescope.model.Device;
import org.telescope.model.Driver;
import org.telescope.model.Geofence;
import org.telescope.model.Group;
import org.telescope.model.Maintenance;
import org.telescope.model.Notification;
import org.telescope.model.Order;
import org.telescope.model.User;
import org.telescope.model.Position;
import org.telescope.reports.model.TripsConfig;
import org.telescope.web.WebServer;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.ext.ContextResolver;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.time.LocalDateTime;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.CodeSource;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.time.temporal.ChronoUnit;

public final class Context {

    private Context() {
    }
    
    private static Logger LOGGER = LoggerFactory.getLogger(Context.class);

    private static Config config;

    public static Config getConfig() {
        return config;
    }

    private static ObjectMapper objectMapper;

    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    private static IdentityManager identityManager;

    public static IdentityManager getIdentityManager() {
        return identityManager;
    }

    private static DataManager dataManager;

    public static DataManager getDataManager() {
        return dataManager;
    }

    private static LdapProvider ldapProvider;

    public static LdapProvider getLdapProvider() {
        return ldapProvider;
    }

    private static MailManager mailManager;

    public static MailManager getMailManager() {
        return mailManager;
    }

    private static MediaManager mediaManager;

    public static MediaManager getMediaManager() {
        return mediaManager;
    }

    private static UsersManager usersManager;

    public static UsersManager getUsersManager() {
        return usersManager;
    }

    private static GroupsManager groupsManager;

    public static GroupsManager getGroupsManager() {
        return groupsManager;
    }

    private static DeviceManager deviceManager;

    public static DeviceManager getDeviceManager() {
        return deviceManager;
    }

    private static ConnectionManager connectionManager;

    public static ConnectionManager getConnectionManager() {
        return connectionManager;
    }
    
    private static WebsocketManager websocketManager;
    
    public static WebsocketManager getWebsocketManager() {
        return websocketManager;
    }

    private static PermissionsManager permissionsManager;

    public static PermissionsManager getPermissionsManager() {
        return permissionsManager;
    }

    public static Geocoder getGeocoder() {
        return Main.getInjector() != null ? Main.getInjector().getInstance(Geocoder.class) : null;
    }

    private static WebServer webServer;

    public static WebServer getWebServer() {
        return webServer;
    }

    private static ServerManager serverManager;

    public static ServerManager getServerManager() {
        return serverManager;
    }

    private static ScheduleManager scheduleManager;

    public static ScheduleManager getScheduleManager() {
        return scheduleManager;
    }

    private static GeofenceManager geofenceManager;

    public static GeofenceManager getGeofenceManager() {
        return geofenceManager;
    }

    private static CalendarManager calendarManager;

    public static CalendarManager getCalendarManager() {
        return calendarManager;
    }

    private static NotificationManager notificationManager;

    public static NotificationManager getNotificationManager() {
        return notificationManager;
    }

    private static NotificatorManager notificatorManager;

    public static NotificatorManager getNotificatorManager() {
        return notificatorManager;
    }

    private static VelocityEngine velocityEngine;

    public static VelocityEngine getVelocityEngine() {
        return velocityEngine;
    }

    private static Client client = ClientBuilder.newClient();

    public static Client getClient() {
        return client;
    }

    private static EventForwarder eventForwarder;

    public static EventForwarder getEventForwarder() {
        return eventForwarder;
    }

    private static AttributesManager attributesManager;

    public static AttributesManager getAttributesManager() {
        return attributesManager;
    }

    private static DriversManager driversManager;

    public static DriversManager getDriversManager() {
        return driversManager;
    }

    private static CommandsManager commandsManager;

    public static CommandsManager getCommandsManager() {
        return commandsManager;
    }

    private static MaintenancesManager maintenancesManager;

    public static MaintenancesManager getMaintenancesManager() {
        return maintenancesManager;
    }

    private static OrderManager orderManager;

    public static OrderManager getOrderManager() {
        return orderManager;
    }

    private static SmsManager smsManager;

    public static SmsManager getSmsManager() {
        return smsManager;
    }

    private static TripsConfig tripsConfig;

    public static TripsConfig getTripsConfig() {
        return tripsConfig;
    }

    private static ModelManager modelManager;

    public static ModelManager getModelManager() {
        return modelManager;
    }

    private static WebsocketDriver websocketDriver;

    public static WebsocketDriver getWebsocketDriver() {
        return websocketDriver;
    }

    public static TripsConfig initTripsConfig() {
        return new TripsConfig(
                config.getLong(Keys.REPORT_TRIP_MINIMAL_TRIP_DISTANCE),
                config.getLong(Keys.REPORT_TRIP_MINIMAL_TRIP_DURATION) * 1000,
                config.getLong(Keys.REPORT_TRIP_MINIMAL_PARKING_DURATION) * 1000,
                config.getLong(Keys.REPORT_TRIP_MINIMAL_NO_DATA_DURATION) * 1000,
                config.getBoolean(Keys.REPORT_TRIP_USE_IGNITION),
                config.getBoolean(Keys.EVENT_MOTION_PROCESS_INVALID_POSITIONS),
                config.getDouble(Keys.MOTION_SPEED_THRESHOLD));
    }

    private static class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

        @Override
        public ObjectMapper getContext(Class<?> clazz) {
            return objectMapper;
        }

    }

    public static void init(String configFile) throws Exception {

        try {
            config = new Config(configFile);
            Log.setupLogger(config);
        } catch (Exception e) {
            config = new Config();
            Log.setupDefaultLogger();
            throw e;
        }

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new SanitizerModule());
        objectMapper.registerModule(new JSR353Module());
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        objectMapper.setConfig(
                objectMapper.getSerializationConfig().without(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));

        client = ClientBuilder.newClient().register(new ObjectMapperContextResolver());

        if (config.hasKey(Keys.DATABASE_URL)) {
            dataManager = new DataManager(config);
        }

        if (config.hasKey(Keys.LDAP_URL)) {
            ldapProvider = new LdapProvider(config);
        }

        mailManager = new MailManager();

        mediaManager = new MediaManager(config.getString(Keys.MEDIA_PATH));

        if (dataManager != null) {
            usersManager = new UsersManager(dataManager);
            groupsManager = new GroupsManager(dataManager);
            deviceManager = new DeviceManager(dataManager);
        }

        identityManager = deviceManager;

        if (config.hasKey(Keys.WEB_PORT)) {
            webServer = new WebServer(config);
        }

        permissionsManager = new PermissionsManager(dataManager, usersManager);

        connectionManager = new ConnectionManager();
        
        websocketManager = new WebsocketManager();

        tripsConfig = initTripsConfig();

        if (config.hasKey(Keys.SMS_HTTP_URL)) {
            smsManager = new HttpSmsClient();
        } else if (config.hasKey(Keys.SMS_AWS_REGION)) {
            smsManager = new SnsSmsClient();
        }

        initEventsModule();

        serverManager = new ServerManager();
        scheduleManager = new ScheduleManager();

        if (config.hasKey(Keys.EVENT_FORWARD_URL)) {
            eventForwarder = new EventForwarder();
        }

        attributesManager = new AttributesManager(dataManager);

        driversManager = new DriversManager(dataManager);

        commandsManager = new CommandsManager(dataManager, config.getBoolean(Keys.COMMANDS_QUEUEING));

        orderManager = new OrderManager(dataManager);
        
        switch (getConfig().getString(Keys.WEBSOCKET_DRIVER)) {
            case "pusher":
                websocketDriver = new Pusher();
            break;
            default:
                websocketDriver = null;
        }

        modelManager = new ModelManager();
    }

    private static void initEventsModule() {

        geofenceManager = new GeofenceManager(dataManager);
        calendarManager = new CalendarManager(dataManager);
        maintenancesManager = new MaintenancesManager(dataManager);
        notificationManager = new NotificationManager(dataManager);
        notificatorManager = new NotificatorManager();
        Properties velocityProperties = new Properties();
        velocityProperties.setProperty("file.resource.loader.path",
                Context.getConfig().getString("templates.rootPath", "templates") + "/");
        velocityProperties.setProperty("runtime.log.logsystem.class",
                "org.apache.velocity.runtime.log.NullLogChute");

        String address;
        try {
            address = config.getString(Keys.WEB_ADDRESS, InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            address = "localhost";
        }

        String webUrl = URIUtil.newURI("http", address, config.getInteger(Keys.WEB_PORT), "", "");
        webUrl = Context.getConfig().getString("web.url", webUrl);
        velocityProperties.setProperty("web.url", webUrl);

        velocityEngine = new VelocityEngine();
        velocityEngine.init(velocityProperties);
    }

    public static void init(IdentityManager testIdentityManager, MediaManager testMediaManager) {
        config = new Config();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR353Module());
        client = ClientBuilder.newClient().register(new ObjectMapperContextResolver());
        identityManager = testIdentityManager;
        mediaManager = testMediaManager;
    }

    public static <T extends BaseModel> BaseObjectManager<T> getManager(Class<T> clazz) {
        if (clazz.equals(Device.class)) {
            return (BaseObjectManager<T>) deviceManager;
        } else if (clazz.equals(Group.class)) {
            return (BaseObjectManager<T>) groupsManager;
        } else if (clazz.equals(User.class)) {
            return (BaseObjectManager<T>) usersManager;
        } else if (clazz.equals(Calendar.class)) {
            return (BaseObjectManager<T>) calendarManager;
        } else if (clazz.equals(Attribute.class)) {
            return (BaseObjectManager<T>) attributesManager;
        } else if (clazz.equals(Geofence.class)) {
            return (BaseObjectManager<T>) geofenceManager;
        } else if (clazz.equals(Driver.class)) {
            return (BaseObjectManager<T>) driversManager;
        } else if (clazz.equals(Command.class)) {
            return (BaseObjectManager<T>) commandsManager;
        } else if (clazz.equals(Maintenance.class)) {
            return (BaseObjectManager<T>) maintenancesManager;
        } else if (clazz.equals(Notification.class)) {
            return (BaseObjectManager<T>) notificationManager;
        } else if (clazz.equals(Order.class)) {
            return (BaseObjectManager<T>) orderManager;
        }
        return null;
    }
    
    public static void event(Event event) {
        String data = getWebsocketDriver().buildMessage(event.channel(), event.event(), event.data());
        LOGGER.info("Broadcast Message: " + data);
        Context.getWebsocketManager().broadcast(event.channel(), data);
    }
    
    public static void takeDatabaseBackup() {
        try {
            CodeSource codeSource = Context.class.getProtectionDomain().getCodeSource();
            File jarFile = new File(codeSource.getLocation().toURI().getPath());
            String jarDir = jarFile.getParentFile().getPath();
        
            String dbName = getConfig().getString(Keys.DATABASE_NAME);
            String dbUser = getConfig().getString(Keys.DATABASE_USER);
            String dbPass = getConfig().getString(Keys.DATABASE_PASSWORD);
        
            String folderPath = jarDir + "/backup";
            File f1 = new File(folderPath);
            if (!f1.exists()) {f1.mkdir();}
        
            String fileName = "backup_" + LocalDateTime.now() + ".sql";
            String savePath = jarDir + "/backup/" + fileName;
            String executeCmd = "mysqldump -h 188.166.165.165 -u" + dbUser + " -p" + dbPass + " " + dbName + " -r " + savePath;

            executeCommand(executeCmd);
            refineDatabase();
            AzureStorage.uploadFile(savePath, dbName + "/" + fileName);
            executeCommand("rm " + savePath);
            LOGGER.info("Database backup completed successfully");
            
        } catch (URISyntaxException e) {
            LOGGER.error("Error when taking database backup" + e.getMessage(), e);
        }
        //AzureStorage.listFiles("backups");
    }
    
    private static void executeCommand(String executeCmd) {
        try {
            Process runtimeProcess = Runtime.getRuntime().exec(executeCmd);
            int processComplete = runtimeProcess.waitFor();
        
            if (processComplete == 0) {
                LOGGER.info("Command: " + executeCmd + " executed successfully");
            } else {
                LOGGER.info("Command: " + executeCmd + "failed with process return : " + Integer.toString(processComplete));
                InputStream error = runtimeProcess.getErrorStream();
                BufferedReader bufferReader = new BufferedReader(new InputStreamReader(error));
                String line;
                if ((line = bufferReader.readLine()) != null) {LOGGER.info("Failure output: " + line);}
            }
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Error when executing command: " + executeCmd + " Error: " + e.getMessage(), e);
        }  
    }
    
    private static void refineDatabase() {
        String positionTable = DataManager.getObjectsTableName(Position.class);
        String eventTable = DataManager.getObjectsTableName(org.telescope.model.Event.class);
        long millisec = new Date().toInstant().minus(30, ChronoUnit.DAYS).toEpochMilli();
        Timestamp sinceMonths = new Timestamp(millisec);
        try {
            QueryBuilder.create(getDataManager().getDataSource(), "DELETE FROM " + positionTable +
            " WHERE servertime < '" + sinceMonths + "'").executeUpdate();
            QueryBuilder.create(getDataManager().getDataSource(), "DELETE FROM " + eventTable +
            " WHERE eventtime < '" + sinceMonths + "' OR eventtime IS NULL").executeUpdate();
            LOGGER.info("Context.refineDatabase: Database refined successfully");
        } catch (SQLException e) {
            LOGGER.error("Can not refine database: " + positionTable + " or table: " + eventTable, e);
        }
    }
    
}
