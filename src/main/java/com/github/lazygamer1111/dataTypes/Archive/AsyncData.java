package com.github.lazygamer1111.dataTypes.Archive;

import org.slf4j.LoggerFactory;
import org.jctools.queues.SpscArrayQueue;

import java.util.Objects;

/**
 * Thread-safe asynchronous data container with change notification.
 * 
 * This class provides a thread-safe way to pass data between threads with
 * change notification capabilities. It uses a Single-Producer/Single-Consumer queue
 * to store values and provides both blocking and non-blocking methods to retrieve data.
 * 
 * The class maintains a "changed" flag to indicate when new data is available,
 * allowing consumers to wait for changes rather than polling continuously.
 *
 * @param <T> the type of data stored in this container
 * @author lazygamer1111
 * @version 1.0
 * @since 2025-11-06
 */
public class AsyncData<T> {
    /**
     * Flag indicating whether the data has changed since it was last retrieved.
     * This is used for the blocking getData method to know when to wait.
     */
    private boolean changed = true;

    /**
     * Queue storing the data values.
     * This is a Single-Producer/Single-Consumer queue that is optimized for
     * the case where one thread produces values and another thread consumes them.
     */
    private final SpscArrayQueue<T> value;

    /**
     * Constructs a new AsyncData instance.
     * 
     * Initializes the internal queue with a capacity of 10 elements.
     * The changed flag is initially set to true to indicate that
     * data is available to be read.
     */
    public AsyncData() {
        this.value = new SpscArrayQueue<>(10);
    }

    /**
     * Retrieves data in a blocking manner, waiting for changes if necessary.
     * 
     * This method blocks the calling thread until new data is available
     * (when the changed flag is true). Once data is available, it resets
     * the changed flag and returns the next value from the queue.
     * 
     * This method is synchronized to ensure thread safety when waiting
     * and being notified of changes.
     *
     * @return the next data value from the queue, never null
     * @throws NullPointerException if the queue is empty when polled
     */
    public synchronized Object getDataBlocking() {
        try {
            // If no changes have occurred, wait for notification
            if (!isChanged()) {
                wait();
            }
            // Reset the changed flag
            changed = false;
            // Get and return the next value from the queue (throws NPE if empty)
            return Objects.requireNonNull(value.poll());
        } catch (InterruptedException e) {
            // Log the interruption but continue
            LoggerFactory.getLogger(AsyncData.class).error("Interrupted", e);
        }
        return null;
    }

    /**
     * Retrieves data in a non-blocking, busy-waiting manner.
     * 
     * This method continuously polls the queue until a non-null value
     * is available. It does not check or modify the changed flag.
     * 
     * WARNING: This method will enter an infinite loop if no data
     * is ever added to the queue.
     *
     * @return the next data value from the queue, never null
     */
    public Object getData() {
        // Continuously poll until data is available
        while (true) {
            Object uData = value.poll();
            if (uData != null) {
                return uData;
            }
            // No yield or sleep, so this is a busy-wait loop
        }
    }

    /**
     * Adds new data to the queue and signals waiting threads.
     * 
     * This method adds the provided data to the queue and marks
     * the data as changed, notifying any threads that are waiting
     * in the getDataBlocking method.
     *
     * @param data the data to add to the queue
     */
    public void setData(Object data) {
        // Add the data to the queue
        this.value.offer((T) data);
        // Mark as changed and notify waiting threads
        setChanged();
    }

    /**
     * Checks if the data has changed since it was last retrieved.
     *
     * @return true if the data has changed, false otherwise
     */
    private boolean isChanged() {
        return changed;
    }

    /**
     * Marks the data as changed and notifies waiting threads.
     * 
     * This method is synchronized to ensure thread safety when
     * notifying waiting threads.
     */
    private synchronized void setChanged() {
        // Mark as changed
        this.changed = true;
        // Notify all waiting threads
        notifyAll();
    }
}
