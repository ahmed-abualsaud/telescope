
package org.telescope.database;

import org.telescope.model.Maintenance;

public class MaintenancesManager extends ExtendedObjectManager<Maintenance> {

    public MaintenancesManager(DataManager dataManager) {
        super(dataManager, Maintenance.class);
    }

}
