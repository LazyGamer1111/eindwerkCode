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
 * The type Main.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final ArrayList<Thread> threads = new ArrayList<>();
    static volatile int[] controllerData = new int[14];
    static boolean DEBUG = false;
    public static ESC esc;

    static{
        System.loadLibrary("native");
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) throws Exception {
//        ESC esc = new ESC(4, 150);
//        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
//
//        while(true){
//            System.out.println("Enter number");
//            System.out.println("SM" + esc.sm);
//
//            String numStr = myObj.nextLine();
//            int num = Integer.parseInt(numStr);
//
//            esc.put(esc.sm, (short) num);
//        }

        esc = new ESC(4, 150);

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
     * Create threads.
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
