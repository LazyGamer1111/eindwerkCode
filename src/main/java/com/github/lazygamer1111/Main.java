package com.github.lazygamer1111;


import com.github.lazygamer1111.dataTypes.ControllerData;
import com.github.lazygamer1111.threads.IOThread;
import com.github.lazygamer1111.threads.SerialThread;
import com.github.lazygamer1111.dataTypes.AsyncData;
import com.github.lazygamer1111.dataTypes.BluetoothData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * The type Main.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final ArrayList<Thread> threads = new ArrayList<>();
    static volatile int[] controllerData = new int[14];

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
    private static void createThreads() {

        Thread io = new IOThread(controllerData);
        threads.add(io);
        Thread serial = new SerialThread(controllerData);
        threads.add(serial);

        for (Thread thread : threads) {
            thread.start();
        }
    }
}
