package com.github.LazyGamer1111;


import com.github.LazyGamer1111.threads.IOThread;
import com.github.LazyGamer1111.threads.SerialThread;
import com.github.LazyGamer1111.dataTypes.AsyncData;
import com.github.LazyGamer1111.dataTypes.BluetoothData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

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
     */
    public static void createThreads() {
        AsyncData<BluetoothData> bluetoothData = new AsyncData<>(new BluetoothData());
        Thread io = new IOThread(bluetoothData);
        threads.add(io);
        Thread serial = new SerialThread(bluetoothData);
        threads.add(serial);

        for (Thread thread : threads) {
            thread.start();
        }
    }
}
