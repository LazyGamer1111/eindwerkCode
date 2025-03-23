package com.github.LazyGamer1111.dataTypes;

import org.slf4j.LoggerFactory;

public class AsyncData<T extends Data> {
    private boolean changed = true;

    private final T value;

    public AsyncData(T value) {
        this.value = value;
    }

    public synchronized Object getDataBlocking() {
        try {
            if (!isChanged()) {
                wait();
            }
            changed = false;
            return value.getData();
        } catch (InterruptedException e) {
            LoggerFactory.getLogger(AsyncData.class).error("Interrupted", e);
        }
        return null;
    }

    public Object getData() {
        if (isChanged()) {
            changed = false;
            return value;
        }
        return null;
    }

    public void setData(Object data) {
        LoggerFactory.getLogger(getClass()).debug("AsyncData setData: {}", data);
        this.value.setData(data);
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
