package com.jianyun.thread.jianyun;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

/**
 * @Auther: chenkui
 * @Date: 2019/2/15 13:06
 * @Description:多线程模式
 */
public class NettyService {
    public static void main(String [] args){
        //服务器端请求保持线程组
        EventLoopGroup boss = new NioEventLoopGroup();
        //任务执行线程组
        EventLoopGroup work = new NioEventLoopGroup();
        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            //请求的策略：未处理的放入队列阻塞等待处理
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            serverBootstrap.group(boss, work).channel(NioServerSocketChannel.class)
                    .childHandler(new NettyService () .new MyReceiveMessageHandler())
                    //是否启用心跳保活机制。在双方TCP套接字建立连接后（即都进入ESTABLISHED状态）
                    // 并且在两个小时左右上层没有任何数据传输的情况下，这套机制才会被激活
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future  = serverBootstrap.bind(8080).sync();
            // 等待服务端Socket关闭
            future.channel().closeFuture().sync();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            boss.shutdownGracefully();
            work.shutdownGracefully();
        }
    }
    /*class MyChannelHandler extends ChannelInitializer<SocketChannel>{
        @Override
        protected void initChannel(SocketChannel socketChannel) {
            ChannelPipeline pipeline = socketChannel.pipeline();
            pipeline.addLast(new HttpServerCodec());
            addAdvanced(pipeline);
            pipeline.addLast(new ChunkedWriteHandler());
            pipeline.addLast(new MyChannelHandler());
        }

        *//**
         * 可以在 HttpServerCodec 之后添加这些 ChannelHandler 进行开启高级特性
         *//*
        private void addAdvanced(ChannelPipeline pipeline){
            if(true) {
                // 对 http 响应结果开启 gizp 压缩
                pipeline.addLast(new HttpContentCompressor());
            }
            if(true) {
                // 将多个HttpRequest组合成一个FullHttpRequest,处理的最大字节数
                pipeline.addLast(new HttpObjectAggregator(10485760));
            }
        }
    }*/
    class MyReceiveMessageHandler extends SimpleChannelInboundHandler<ByteBuf>{
        @Override
        protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) {
            System.out.println(byteBuf.toString(CharsetUtil.UTF_8));
            channelHandlerContext.write(Unpooled.copiedBuffer("Response from server. received your sending message! ", CharsetUtil.UTF_8));
            channelHandlerContext.flush();
        }
    }
}
