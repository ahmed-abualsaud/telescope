

package org.telescope.reports;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.apache.poi.ss.util.WorkbookUtil;
import org.telescope.database.DeviceManager;
import org.telescope.database.IdentityManager;
import org.telescope.model.Device;
import org.telescope.model.Group;
import org.telescope.reports.model.DeviceReport;
import org.telescope.reports.model.StopReport;
import org.telescope.server.Context;
import org.telescope.server.Main;

public final class Stops {

    private Stops() {
    }

    private static Collection<StopReport> detectStops(long deviceId, Date from, Date to) throws SQLException {
        boolean ignoreOdometer = Context.getDeviceManager()
                .lookupAttributeBoolean(deviceId, "report.ignoreOdometer", false, false, true);

        IdentityManager identityManager = Main.getInjector().getInstance(IdentityManager.class);
        DeviceManager deviceManager = Main.getInjector().getInstance(DeviceManager.class);

        return ReportUtils.detectTripsAndStops(
                identityManager, deviceManager, Context.getDataManager().getPositions(deviceId, from, to),
                Context.getTripsConfig(), ignoreOdometer, StopReport.class);
    }

    public static Collection<StopReport> getObjects(
            long userId, Collection<Long> deviceIds, Collection<Long> groupIds,
            Date from, Date to) throws SQLException {
        ReportUtils.checkPeriodLimit(from, to);
        ArrayList<StopReport> result = new ArrayList<>();
        for (long deviceId: ReportUtils.getDeviceList(deviceIds, groupIds)) {
            Context.getPermissionsManager().checkDevice(userId, deviceId);
            result.addAll(detectStops(deviceId, from, to));
        }
        return result;
    }

    public static void getExcel(
            OutputStream outputStream, long userId, Collection<Long> deviceIds, Collection<Long> groupIds,
            Date from, Date to) throws SQLException, IOException {
        ReportUtils.checkPeriodLimit(from, to);
        ArrayList<DeviceReport> devicesStops = new ArrayList<>();
        ArrayList<String> sheetNames = new ArrayList<>();
        for (long deviceId: ReportUtils.getDeviceList(deviceIds, groupIds)) {
            Context.getPermissionsManager().checkDevice(userId, deviceId);
            Collection<StopReport> stops = detectStops(deviceId, from, to);
            DeviceReport deviceStops = new DeviceReport();
            Device device = Context.getIdentityManager().getById(deviceId);
            deviceStops.setDeviceName(device.getName());
            sheetNames.add(WorkbookUtil.createSafeSheetName(deviceStops.getDeviceName()));
            if (device.getGroupId() != 0) {
                Group group = Context.getGroupsManager().getById(device.getGroupId());
                if (group != null) {
                    deviceStops.setGroupName(group.getName());
                }
            }
            deviceStops.setObjects(stops);
            devicesStops.add(deviceStops);
        }
        String templatePath = Context.getConfig().getString("report.templatesPath",
                "templates/export/");
        try (InputStream inputStream = new FileInputStream(templatePath + "/stops.xlsx")) {
            org.jxls.common.Context jxlsContext = ReportUtils.initializeContext(userId);
            jxlsContext.putVar("devices", devicesStops);
            jxlsContext.putVar("sheetNames", sheetNames);
            jxlsContext.putVar("from", from);
            jxlsContext.putVar("to", to);
            ReportUtils.processTemplateWithSheets(inputStream, outputStream, jxlsContext);
        }
    }

}
