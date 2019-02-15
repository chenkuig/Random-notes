package com.jianyun.thread.jianyun;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;

/**
 * @Auther: chenkui
 * @Date: 2019/2/15 17:41
 * @Description:
 */
public class NettyClient {
    public static void main(String [] args){
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress("127.0.0.1", 8080))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(new NettyClient().new MySendMessageHandler());
                        }
                    });

            ChannelFuture f = b.connect().sync();
            if (f.channel().isActive()) {
                f.channel().writeAndFlush(Unpooled.copiedBuffer("Hello Casper!", CharsetUtil.UTF_8));
            }
            Thread.sleep(1000);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            group.shutdownGracefully();
        }
    }
    class MySendMessageHandler extends SimpleChannelInboundHandler<ByteBuf> {
        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
            System.out.println("返回的结果："+byteBuf.toString(CharsetUtil.UTF_8));
        }
    }
}
