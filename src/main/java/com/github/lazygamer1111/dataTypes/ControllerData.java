package com.github.lazygamer1111.dataTypes;

import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicIntegerArray;

public class ControllerData extends Data {
    AtomicIntegerArray channels = new AtomicIntegerArray(14);

    @Override
    public Object getData() {
        Integer[] get = new Integer[14];
        for (int i = 0; i < 14; i++) {
            get[i] = channels.get(i);
        }
        return get;
    }

    @Override
    public void setData(Object data) {
        if (data instanceof Integer[] data1) {
            for (int i = 0; i < 14; i++) {
                channels.set(i, data1[i]);
            }
        }
    }
}
