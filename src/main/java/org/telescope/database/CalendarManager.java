
package org.telescope.database;

import org.telescope.model.Calendar;

public class CalendarManager extends SimpleObjectManager<Calendar> {

    public CalendarManager(DataManager dataManager) {
        super(dataManager, Calendar.class);
    }

}
