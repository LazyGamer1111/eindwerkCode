package com.github.LazyGamer1111;


import com.github.LazyGamer1111.Threads.IOThread;
import com.github.LazyGamer1111.Threads.SerialThread;
import com.github.LazyGamer1111.dataTypes.AsyncData;
import com.github.LazyGamer1111.dataTypes.BluetoothData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * The class Main.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final ArrayList<Thread> threads = new ArrayList<>();
    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        log.info("Hello World");


        try {
            createThreads();
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
        }


    }

    /**
     * Create threads.
     *
     * @throws NoSuchMethodException     the no such method exception
     * @throws InvocationTargetException the invocation target exception
     * @throws InstantiationException    the instantiation exception
     * @throws IllegalAccessException    the illegal access exception
     */
    public static void createThreads() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        AsyncData<BluetoothData> bluetoothData = new AsyncData<>(new BluetoothData());
        Thread io = new IOThread(bluetoothData);
        threads.add(io);
        Thread serial = new SerialThread(bluetoothData);
        threads.add(serial);

        for (int i = 0; i < threads.size(); i++) {
            threads.get(i).start();
        }
    }
}
