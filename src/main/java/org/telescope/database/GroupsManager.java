
package org.telescope.database;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.telescope.model.Group;
import org.telescope.server.Context;

public class GroupsManager extends BaseObjectManager<Group> implements ManagableObjects {

    public GroupsManager(DataManager dataManager) {
        super(dataManager, Group.class);
    }

    private void checkGroupCycles(Group group) {
        Set<Long> groups = new HashSet<>();
        while (group != null) {
            if (groups.contains(group.getId())) {
                throw new IllegalArgumentException("Cycle in group hierarchy");
            }
            groups.add(group.getId());
            group = getById(group.getGroupId());
        }
    }

    @Override
    public Set<Long> getAllItems() {
        Set<Long> result = super.getAllItems();
        /*if (result.isEmpty()) {
            refreshItems();
            result = super.getAllItems();
        }*/
        return result;
    }

    @Override
    protected void addNewItem(Group group) {
        checkGroupCycles(group);
        super.addNewItem(group);
    }

    @Override
    public void updateItem(Group group) throws SQLException {
        checkGroupCycles(group);
        super.updateItem(group);
    }

    @Override
    public Set<Long> getUserItems(long userId) {
        if (Context.getPermissionsManager() != null) {
            return Context.getPermissionsManager().getGroupPermissions(userId);
        } else {
            return new HashSet<>();
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

}
