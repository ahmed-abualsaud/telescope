
package org.telescope.database;

import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.model.Device;
import org.telescope.model.Group;
import org.telescope.model.Permission;
import org.telescope.server.Context;
import org.telescope.model.BaseModel;

public abstract class ExtendedObjectManager<T extends BaseModel> extends SimpleObjectManager<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedObjectManager.class);

    private final Map<Long, Set<Long>> deviceItems = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> deviceItemsWithGroups = new ConcurrentHashMap<>();
    private final Map<Long, Set<Long>> groupItems = new ConcurrentHashMap<>();

    protected ExtendedObjectManager(DataManager dataManager, Class<T> baseClass) {
        super(dataManager, baseClass);
        refreshExtendedPermissions();
    }

    public final Set<Long> getGroupItems(long groupId) {
        try {
            readLock();
            Set<Long> result = groupItems.get(groupId);
            if (result != null) {
                return new HashSet<>(result);
            } else {
                return new HashSet<>();
            }
        } finally {
            readUnlock();
        }
    }

    public final Set<Long> getDeviceItems(long deviceId) {
        try {
            readLock();
            Set<Long> result = deviceItems.get(deviceId);
            if (result != null) {
                return new HashSet<>(result);
            } else {
                return new HashSet<>();
            }
        } finally {
            readUnlock();
        }
    }

    public Set<Long> getAllDeviceItems(long deviceId) {
        try {
            readLock();
            Set<Long> result = deviceItemsWithGroups.get(deviceId);
            if (result != null) {
                return new HashSet<>(result);
            } else {
                return new HashSet<>();
            }
        } finally {
            readUnlock();
        }
    }

    @Override
    public void removeItem(long itemId) throws SQLException {
        super.removeItem(itemId);
        refreshExtendedPermissions();
    }

    public void refreshExtendedPermissions() {
        /*if (getDataManager() != null) {
            try {
                Collection<Permission> databaseGroupPermissions =
                        getDataManager().getPermissions(Group.class, getBaseClass());

                Collection<Permission> databaseDevicePermissions =
                        getDataManager().getPermissions(Device.class, getBaseClass());

                writeLock();

                groupItems.clear();
                deviceItems.clear();
                deviceItemsWithGroups.clear();

                for (Permission groupPermission : databaseGroupPermissions) {
                    groupItems
                            .computeIfAbsent(groupPermission.getOwnerId(), key -> new HashSet<>())
                            .add(groupPermission.getPropertyId());
                }

                for (Permission devicePermission : databaseDevicePermissions) {
                    deviceItems
                            .computeIfAbsent(devicePermission.getOwnerId(), key -> new HashSet<>())
                            .add(devicePermission.getPropertyId());
                    deviceItemsWithGroups
                            .computeIfAbsent(devicePermission.getOwnerId(), key -> new HashSet<>())
                            .add(devicePermission.getPropertyId());
                }

                for (Device device : Context.getDeviceManager().getAllDevices()) {
                    long groupId = device.getGroupId();
                    while (groupId > 0) {
                        deviceItemsWithGroups
                                .computeIfAbsent(device.getId(), key -> new HashSet<>())
                                .addAll(groupItems.getOrDefault(groupId, new HashSet<>()));
                        Group group = Context.getGroupsManager().getById(groupId);
                        groupId = group != null ? group.getGroupId() : 0;
                    }
                }

            } catch (SQLException | ClassNotFoundException error) {
                LOGGER.warn("Refresh permissions error", error);
            } finally {
                writeUnlock();
            }
        }*/
    }
}
