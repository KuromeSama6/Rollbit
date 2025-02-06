package moe.ku6.rollbit.util;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

@Slf4j
public class PooledScheduler {
    private final ScheduledExecutorService executorService;

    private int counter = 0;
    private final Map<Integer, ScheduledFuture<?>> tasks = new HashMap<>();

    public PooledScheduler(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    public int Add(Runnable runnable, long delay) {
        var id = counter++;
        tasks.put(id, executorService.schedule(() -> CallRunnable(id, runnable), delay, java.util.concurrent.TimeUnit.MILLISECONDS));
        return id;
    }

    public int AddRepeated(Runnable runnable, long delay, long period) {
        var id = counter++;
        tasks.put(id, executorService.scheduleAtFixedRate(() -> CallRunnable(id, runnable), delay, period, java.util.concurrent.TimeUnit.MILLISECONDS));
        return id;
    }

    public void Cancel(int id) {
        var task = tasks.get(id);
        if (task != null && (!task.isCancelled() || !task.isDone())) {
            task.cancel(true);
            tasks.remove(id);
        }
    }

    public void Free() {
        tasks.forEach((k, v) -> v.cancel(true));
        tasks.clear();
    }

    private void CallRunnable(int id, Runnable runnable) {
        try {
            runnable.run();
        } catch (Throwable e) {
            log.error("Error in scheduled task #{}", id);
            e.printStackTrace();
        }
    }
}
