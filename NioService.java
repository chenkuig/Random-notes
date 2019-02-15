package com.jianyun.thread.jianyun;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @Auther: chenkui
 * @Date: 2019/2/15 15:13
 * @Description:非阻塞IO流事例(非阻塞指的是读与写不需要等待)，主要思想是将channel注册到Selector，然后采取轮询方式检验key值，并作相应处理
 * NIO采用事件驱动模型，即：Reactor
 * NIO不需要内存交换，采用直接内存
 * NIO中的三个基础术语：buffer(缓存区)，channel(通道)，Selector(选择器)
 * 对应的channel有以下常用的4种：
 * FileChannel（文件）
 * SocketChanel（客户端）
 * ServerSocketChannel（服务器端）
 * DatagramChannel 基于UDP传输协议的一种通道，上面的Socket是基于TCP/ip协议的传输的通道
 */
public class NioService {
    public static void main(String [] args){
        try{
            //创建socketChannel服务器端
            ServerSocketChannel serverSocketChannel =  ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(8080));
            // 将其注册到 Selector 中，监听 OP_ACCEPT 事件
            serverSocketChannel.configureBlocking(false);
            //创建selector，并将serverSocketChannel注册到selector上
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while(true){
                // 需要不断地去调用 select() 方法获取最新的准备好的通道
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
                //获取准备好数据的通道,其中SelectionKey是带有状态(accept , read , write)的channel
                Set<SelectionKey> readyKeys = selector.keys();
                Iterator<SelectionKey> iterator = readyKeys.iterator();
                while(iterator.hasNext()){
                    SelectionKey selectionKey = iterator.next();
                    if (selectionKey.isAcceptable()){
                        // 有已经接受的新的到服务端的连接
                        SocketChannel socketChannel = serverSocketChannel.accept();
                        if (socketChannel!=null){
                            // 有新的连接并不代表这个通道就有数据，
                            // 这里将这个新的 SocketChannel 注册到 Selector，监听 OP_READ 事件，等待数据
                            socketChannel.configureBlocking(false);
                            socketChannel.register(selector, SelectionKey.OP_READ);
                        }
                    }else if (selectionKey.isReadable()){
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                        int num ;
                        while((num=socketChannel.read(readBuffer))>0){
                            // 读取 Buffer 内容之前先 flip 一下
                            readBuffer.flip();
                            // 提取 Buffer 中的数据
                            byte[] bytes = new byte[num];
                            readBuffer.get(bytes);
                            String re = new String(bytes, "UTF-8");
                            System.out.println("收到请求：" + re);
                        }
                        if (num == -1) {
                            // -1 代表连接已经关闭
                            socketChannel.close();
                        }else{
                            //重新注册到写的通道上
                            socketChannel.register(selector, SelectionKey.OP_WRITE);
                        }
                    }else if (selectionKey.isWritable()){
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer buffer = ByteBuffer.wrap("你好呀，已经接收到了相关数据".getBytes());
                        socketChannel.write(buffer);
                        socketChannel.write(buffer);
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
