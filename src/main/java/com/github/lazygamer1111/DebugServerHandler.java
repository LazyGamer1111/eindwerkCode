package com.github.lazygamer1111;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        final ByteBuf buf = ctx.alloc().buffer(1024);
        for (int val : Main.controllerData) {
            buf.writeInt(val);
        }

        final ChannelFuture f = ctx.writeAndFlush(buf);
        f.addListener((ChannelFutureListener) future -> {
            assert f == future;
            ctx.close();
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Logger log = LoggerFactory.getLogger(ctx.channel().remoteAddress().toString());
        log.error(cause.getMessage(), cause);


        ctx.close();
    }
}
