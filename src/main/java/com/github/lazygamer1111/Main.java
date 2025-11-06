package com.github.lazygamer1111;


import com.github.lazygamer1111.components.output.ESC;
import com.github.lazygamer1111.threads.IOThread;
import com.github.lazygamer1111.threads.PIOThread;
import com.github.lazygamer1111.threads.SerialThread;
import io.avaje.applog.AppLog;
import io.avaje.config.Config;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Scanner;

/**
 * Main application class for the Eindwerk project.
 * 
 * This class serves as the entry point for the application which is designed to run on a Raspberry Pi.
 * It manages threads for I/O operations and serial communication, and provides functionality
 * for controlling hardware components like ESCs (Electronic Speed Controllers) and servos.
 * 
 * @author lazygamer1111
 * @version 1.0
 * @since 2025-11-02
 */
public class Main {

    /** Logger for this class */
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    /** List to keep track of all created threads */
    private static final ArrayList<Thread> threads = new ArrayList<>();
    
    /** 
     * Shared array for controller data that is read from serial port and used by IO operations.
     * This array is accessed by multiple threads and thus declared volatile.
     */
    static volatile int[] controllerData = new int[14];
    
    /** Flag to enable/disable debug mode and start the debug server */
    static boolean DEBUG = false;
    public static ESC esc;

    /**
     * Static initializer block to load the native library required for hardware control.
     * This library contains native methods used by components like ESC.
     */
    static {
        System.loadLibrary("native");
    }

    /**
     * The entry point of the application.
     * 
     * Currently configured to create an ESC instance and accept user input to control it.
     * The commented-out code shows the intended functionality to initialize threads for
     * I/O and serial communication, and optionally start a debug server.
     *
     * @param args command line arguments - when debug mode is enabled, the first argument can specify the debug server port
     * @throws Exception if an error occurs during thread creation or debug server initialization
     */
    public static void main(String[] args) throws Exception {
        // DO NOT MOVE! SEGFAULT IN NATIVE LIB IF MOVED!
        // still don't know why tho |:
        esc = new ESC(4, 300);

        DEBUG = Config.getBool("debug");

        try {
            createThreads();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        if (DEBUG) {
            int port = 8080;
            if (args.length > 0) {
                port = Integer.parseInt(args[0]);
            }

            new DebugServer(port).run();
        }
    }

    /**
     * Creates and starts the application's worker threads.
     * 
     * This method initializes two threads:
     * 1. IOThread - handles GPIO/PWM operations for servo control
     * 2. SerialThread - reads controller data from the serial port
     * 
     * Both threads share access to the controllerData array for communication.
     */
    private static void createThreads() {
        Thread io = new IOThread(controllerData);
        threads.add(io);
        Thread serial = new SerialThread(controllerData);
        threads.add(serial);
        Thread pio = new PIOThread(controllerData, esc);
        threads.add(pio);

        for (Thread thread : threads) {
            thread.start();
        }
    }
}
