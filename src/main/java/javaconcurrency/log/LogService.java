package javaconcurrency.log;

import net.jcip.annotations.GuardedBy;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogService {
    private static final int CAPACITY = 100;
    private final BlockingQueue<String> blockingQueue;
    private final LoggerThread loggerThread;
    @GuardedBy("this")
    private boolean isShutdown;
    @GuardedBy("this")
    private int reservations;

    public LogService(PrintWriter printWriter) {
        this.blockingQueue = new LinkedBlockingQueue<String>(CAPACITY);
        this.loggerThread = new LoggerThread(printWriter);
    }

    public void start() {
        loggerThread.start();
    }

    public void log(String msg) throws InterruptedException {
        synchronized (this) {
            if (isShutdown) {
                throw new IllegalStateException();
            }
            ++reservations;
        }
        blockingQueue.put(msg);
    }

    class LoggerThread extends Thread {
        private final PrintWriter printWriter;

        LoggerThread(PrintWriter printWriter) {
            this.printWriter = printWriter;
        }

        @Override
        public void run() {
            try {
                while(true) {
                    printWriter.println(blockingQueue.take());
                }
            } catch (InterruptedException ignored) {
            } finally {
                printWriter.close();
            }
        }
    }
}
