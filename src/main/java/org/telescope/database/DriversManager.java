
package org.telescope.database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.telescope.model.Driver;

public class DriversManager extends ExtendedObjectManager<Driver> {

    private Map<String, Driver> driversByUniqueId;

    public DriversManager(DataManager dataManager) {
        super(dataManager, Driver.class);
        try {
            writeLock();
            if (driversByUniqueId == null) {
                driversByUniqueId = new ConcurrentHashMap<>();
            }
        } finally {
            writeUnlock();
        }
    }

    private void addByUniqueId(Driver driver) {
        try {
            writeLock();
            if (driversByUniqueId == null) {
                driversByUniqueId = new ConcurrentHashMap<>();
            }
            driversByUniqueId.put(driver.getUniqueId(), driver);
        } finally {
            writeUnlock();
        }
    }

    private void removeByUniqueId(String driverUniqueId) {
        try {
            writeLock();
            if (driversByUniqueId == null) {
                driversByUniqueId = new ConcurrentHashMap<>();
            }
            driversByUniqueId.remove(driverUniqueId);
        } finally {
            writeUnlock();
        }
    }

    @Override
    protected void addNewItem(Driver driver) {
        super.addNewItem(driver);
        //addByUniqueId(driver);
    }

    @Override
    protected void updateCachedItem(Driver driver) {
        Driver cachedDriver = getById(driver.getId());
        cachedDriver.setName(driver.getName());
        if (!driver.getUniqueId().equals(cachedDriver.getUniqueId())) {
            removeByUniqueId(cachedDriver.getUniqueId());
            cachedDriver.setUniqueId(driver.getUniqueId());
            //addByUniqueId(cachedDriver);
        }
        cachedDriver.setAttributes(driver.getAttributes());
    }

    @Override
    protected void removeCachedItem(long driverId) {
        Driver cachedDriver = getById(driverId);
        if (cachedDriver != null) {
            String driverUniqueId = cachedDriver.getUniqueId();
            super.removeCachedItem(driverId);
            removeByUniqueId(driverUniqueId);
        }
    }

    public Driver getDriverByUniqueId(String uniqueId) {
        try {
            readLock();
            return driversByUniqueId.get(uniqueId);
        } finally {
            readUnlock();
        }
    }
}
