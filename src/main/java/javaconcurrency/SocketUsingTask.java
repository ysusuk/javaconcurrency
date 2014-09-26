package javaconcurrency;

import net.jcip.annotations.GuardedBy;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.*;

interface CancellableTask<T>
    extends Callable<T> {
    void cancel();

    RunnableFuture<T> newTask();
}

class CancellingExecutor
    extends ThreadPoolExecutor {

    public CancellingExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        if (callable instanceof CancellableTask) {
            return ((CancellableTask) callable).newTask();
        } else {
            return super.newTaskFor(callable);
        }
    }
}

public abstract class SocketUsingTask<T>
    implements CancellableTask<T> {

    @GuardedBy("this")
    private Socket socket;

    protected synchronized void setSocket(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void cancel() {
        try {
            this.socket.close();
        } catch (IOException exc) {
            throw new RuntimeException("Could not cancel socket", exc);
        }
    }

    @Override
    public RunnableFuture<T> newTask() {
        return new FutureTask<T>(this) {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                try {
                    SocketUsingTask.this.cancel();
                } finally {
                    return super.cancel(mayInterruptIfRunning);
                }
            }
        };
    }
}
