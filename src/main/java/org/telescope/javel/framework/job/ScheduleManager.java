
package org.telescope.javel.framework.job;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.telescope.app.job.TaskDeviceInactivityCheck;
import org.telescope.app.job.TaskTakeDatabaseBackup;
import org.telescope.app.job.TaskWebSocketKeepalive;

public class ScheduleManager {

    private ScheduledExecutorService executor;

    public void start() {

        executor = Executors.newSingleThreadScheduledExecutor();

        new TaskDeviceInactivityCheck().schedule(executor);
        new TaskWebSocketKeepalive().schedule(executor);
        new TaskTakeDatabaseBackup().schedule(executor);

    }

    public void stop() {

        if (executor != null) {
            executor.shutdown();
            executor = null;
        }

    }

}
