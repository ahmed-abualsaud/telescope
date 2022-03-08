
package org.telescope.app.job;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.telescope.server.Context;

public class TaskTakeDatabaseBackup implements Runnable {

    private static final long PERIOD_DAYS = 30;

    public void schedule(ScheduledExecutorService executor) {
        executor.scheduleAtFixedRate(this, PERIOD_DAYS, PERIOD_DAYS, TimeUnit.DAYS);
    }

    @Override
    public void run() {
        Context.takeDatabaseBackup();
    }
}
