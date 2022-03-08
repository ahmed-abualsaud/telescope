
package org.telescope.database;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telescope.model.BaseModel;
import org.telescope.model.Permission;
import org.telescope.model.User;
import org.telescope.server.Context;

public abstract class SimpleObjectManager<T extends BaseModel> extends BaseObjectManager<T>
        implements ManagableObjects {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleObjectManager.class);

    private Map<Long, Set<Long>> userItems;

    protected SimpleObjectManager(DataManager dataManager, Class<T> baseClass) {
        super(dataManager, baseClass);
    }

    @Override
    public final Set<Long> getUserItems(long userId) {
        try {
            readLock();
            Set<Long> result = userItems.get(userId);
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
    public Set<Long> getManagedItems(long userId) {
        Set<Long> result = getUserItems(userId);
        for (long managedUserId : Context.getUsersManager().getUserItems(userId)) {
            result.addAll(getUserItems(managedUserId));
        }
        return result;
    }

    public final boolean checkItemPermission(long userId, long itemId) {
        return getUserItems(userId).contains(itemId);
    }

    @Override
    public void refreshItems() {
        super.refreshItems();
        refreshUserItems();
    }

    public final void refreshUserItems() {
        /*if (getDataManager() != null) {
            try {
                writeLock();
                userItems = new ConcurrentHashMap<>();
                for (Permission permission : getDataManager().getPermissions(User.class, getBaseClass())) {
                    Set<Long> items = userItems.computeIfAbsent(permission.getOwnerId(), key -> new HashSet<>());
                    items.add(permission.getPropertyId());
                }
            } catch (SQLException | ClassNotFoundException error) {
                LOGGER.warn("Error getting permissions", error);
            } finally {
                writeUnlock();
            }
        }*/
    }

    @Override
    public void removeItem(long itemId) throws SQLException {
        super.removeItem(itemId);
        refreshUserItems();
    }

}
