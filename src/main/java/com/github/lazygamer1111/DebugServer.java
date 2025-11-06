package com.github.lazygamer1111;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * Debug server for remote monitoring and diagnostics.
 * 
 * This class implements a Netty-based TCP server that allows remote connections
 * for debugging and monitoring the application's state. When enabled, it provides
 * real-time access to controller data and system status.
 * 
 * The server uses the Netty framework for non-blocking I/O and delegates request
 * handling to the DebugServerHandler class.
 * 
 * @author lazygamer1111
 * @version 1.0
 * @since 2025-11-02
 */
public class DebugServer {

    /** The TCP port on which the debug server will listen for connections */
    private int port;

    /**
     * Constructs a new debug server that will listen on the specified port.
     *
     * @param port the TCP port number to listen on (e.g., 8080)
     */
    public DebugServer(int port) {
        this.port = port;
    }

    /**
     * Starts the debug server and begins listening for connections.
     * <p>
     * This method initializes the Netty server with appropriate channel handlers
     * and network configuration. It blocks until the server is shut down.
     *
     * @throws Exception if an error occurs during server initialization or operation
     */
    public void run() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap(); // (2)
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class) // (3)
                    .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new DebugServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

            // Bind and start to accept incoming connections.
            ChannelFuture f = b.bind(port).sync(); // (7)

            // Wait until the server socket is closed.
            // In this example, this does not happen, but you can do that to gracefully
            // shut down your server.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
