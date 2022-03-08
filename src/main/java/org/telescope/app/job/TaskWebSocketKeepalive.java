
package org.telescope.app.job;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.telescope.server.Context;

public class TaskWebSocketKeepalive implements Runnable {

    private static final long PERIOD_SECONDS = 55;

    public void schedule(ScheduledExecutorService executor) {
        executor.scheduleAtFixedRate(this, PERIOD_SECONDS, PERIOD_SECONDS, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        Context.getConnectionManager().sendKeepalive();
    }

}
