package com.github.lazygamer1111;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for debug server connections.
 * 
 * This class handles incoming connections to the debug server by sending
 * the current controller data to the client and then closing the connection.
 * It extends Netty's ChannelInboundHandlerAdapter to process channel events.
 * 
 * @author lazygamer1111
 * @version 1.0
 * @since 2025-11-06
 * @see DebugServer
 */
public class DebugServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * Handles a new active connection to the debug server.
     * <p>
     * This method is called when a new client connects to the debug server.
     * It creates a buffer, writes the current controller data values to it,
     * sends the buffer to the client, and then closes the connection.
     *
     * @param ctx the channel handler context
     * @throws Exception if an error occurs during processing
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Allocate a buffer to hold the controller data
        final ByteBuf buf = ctx.alloc().buffer(1024);
        
        // Write each controller data value to the buffer
        for (int val : Main.controllerData) {
            buf.writeInt(val);
        }

        // Send the buffer to the client and close the connection when complete
        final ChannelFuture f = ctx.writeAndFlush(buf);
        f.addListener((ChannelFutureListener) future -> {
            assert f == future;
            ctx.close();
        });
    }

    /**
     * Handles exceptions that occur during channel processing.
     * 
     * This method logs the exception with the client's remote address
     * and closes the connection to prevent resource leaks.
     *
     * @param ctx the channel handler context
     * @param cause the exception that was thrown
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // Create a logger with the client's address for context
        Logger log = LoggerFactory.getLogger(ctx.channel().remoteAddress().toString());
        
        // Log the exception
        log.error(cause.getMessage(), cause);

        // Close the connection to prevent resource leaks
        ctx.close();
    }
}
