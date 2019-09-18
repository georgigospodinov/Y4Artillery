package util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import static util.PrintFormatting.NEW_LINE;
import static util.PrintFormatting.print;

/**
 * Provides methods to log information to files.
 *
 * @version 3.0
 */
public class Logger {

    private static final int FLUSH_PERIOD = 5000;//ms

    private BufferedWriter writer = null;

    /**
     * Used to control the flushing thread.
     *
     * @see Logger#periodicFlush()
     */
    private boolean contentsUpdated = false;

    /**
     * Used to control the flushing thread.
     *
     * @see Logger#periodicFlush()
     */
    private boolean running;

    public Logger(String filename) {
        try {
            writer = new BufferedWriter(new FileWriter(filename));
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        running = true;
        new Thread(this::periodicFlush).start();
    }

    public void close() {
        try {
            writer.close();
            contentsUpdated = false;  // Closing causes a flush, so periodicFlush knows not to flush.
            running = false;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(String line) {
        try {
            writer.write(line + NEW_LINE);
            contentsUpdated = true;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(Exception e) {
        log(e.toString());
        StackTraceElement[] stack = e.getStackTrace();
        for (StackTraceElement element : stack)
            log("\t" + element.toString());
    }

    /**
     * Periodically flushes the log.
     * In case the program crashes before the writer was properly closed,
     * there might be some log to read.
     */
    private void periodicFlush() {
        while (running) {
            try {
                Thread.sleep(FLUSH_PERIOD);
            }
            catch (InterruptedException ignored) {
            }

            if (!contentsUpdated) continue;
            try {
                writer.flush();
            }
            catch (IOException e) {
                print("Could not flush log.");
            }
            contentsUpdated = false;
        }
    }
}
