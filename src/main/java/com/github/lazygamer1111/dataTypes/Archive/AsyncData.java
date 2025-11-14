package com.github.lazygamer1111.dataTypes.Archive;

import org.slf4j.LoggerFactory;
import org.jctools.queues.SpscArrayQueue;

import java.util.Objects;

/**
 * The type Async data.
 *
 * @param <T> the type parameter
 */
public class AsyncData<T> {
    private boolean changed = true;

    private final SpscArrayQueue<T> value;

    /**
     * Instantiates a new Async data.
     *
     */
    public AsyncData() {
        this.value = new SpscArrayQueue<>(10);
    }

    /**
     * Gets data blocking.
     *
     * @return the data blocking
     */
    public synchronized Object getDataBlocking() {
        try {
            if (!isChanged()) {
                wait();
            }
            changed = false;
            return Objects.requireNonNull(value.poll());
        } catch (InterruptedException e) {
            LoggerFactory.getLogger(AsyncData.class).error("Interrupted", e);
        }
        return null;
    }

    /**
     * Gets data.
     *
     * @return the data
     */
    public Object getData() {
        while (true) {
            Object uData = value.poll();
            if (uData != null) {
                return uData;
            }
        }
    }

    /**
     * Sets data.
     *
     * @param data the data
     */
    public void setData(Object data) {
        this.value.offer((T) data);
        setChanged();
    }

    private boolean isChanged() {
        return changed;
    }

    private synchronized void setChanged() {
        this.changed = true;
        notifyAll();
    }
}
