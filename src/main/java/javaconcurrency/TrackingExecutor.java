package javaconcurrency;

import java.util.*;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * which tasks were in progress at shutdown time.
 */
public class TrackingExecutor
    extends AbstractExecutorService {
    private final ExecutorService executorService;
    private final Set<Runnable> tasksCancelledAtShutdown = Collections.synchronizedSet(new HashSet<Runnable>());

    public TrackingExecutor(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public Set<Runnable> getCancelledTasks() {
        if (!executorService.isTerminated()) {
            throw new IllegalStateException();
        }
        return new HashSet<Runnable>(tasksCancelledAtShutdown);
    }

    @Override
    public void execute(final Runnable command) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    command.run();
                } finally {
                    if (isShutdown() && Thread.currentThread().isInterrupted()) {
                        tasksCancelledAtShutdown.add(command);
                    }
                }
            }
        });
    }

    @Override
    public void shutdown() {

    }

    @Override
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }
}
