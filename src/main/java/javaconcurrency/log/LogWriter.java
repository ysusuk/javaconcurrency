package javaconcurrency.log;

import java.io.PrintWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class LogWriter {
    private static final int CAPACITY = 100;
    private final BlockingQueue<String> blockingQueue;
    private final LoggerThread loggerThread;

    public LogWriter(PrintWriter writer) {
        this.blockingQueue = new LinkedBlockingQueue<String>(CAPACITY);
        this.loggerThread = new LoggerThread(writer);
    }

    public void start() {
        loggerThread.start();
    }

    public void log(String msg) throws InterruptedException {
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
