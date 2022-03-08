
package org.telescope.database;

import org.telescope.model.Order;

public class OrderManager extends ExtendedObjectManager<Order> {

    public OrderManager(DataManager dataManager) {
        super(dataManager, Order.class);
    }

}
