package com.github.lazygamer1111;


import com.github.lazygamer1111.threads.IOThread;
import com.github.lazygamer1111.threads.SerialThread;
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

import java.util.ArrayList;

/**
 * The type Main.
 */
public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    private static final ArrayList<Thread> threads = new ArrayList<>();
    static volatile int[] controllerData = new int[14];
    static boolean DEBUG = false;

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) throws Exception {
        log.info("Hello World");

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

        for (Thread thread : threads) {
            thread.start();
        }
    }
}
