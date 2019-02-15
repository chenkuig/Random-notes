package com.jianyun.thread.jianyun;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * @Auther: chenkui
 * @Date: 2019/2/15 15:52
 * @Description:
 */
public class NioClient {
    private static int i =1;
    private static SocketChannel socketChannel= null;
    private static void getConnect(){
        boolean connectState = false;
        try{
            socketChannel = SocketChannel.open();
            connectState= socketChannel.connect(new InetSocketAddress("localhost", 8080));
            System.out.println("第"+i+"此连接，"+(connectState ? "服务器已连接成功！":"服务器已连接失败！"));
            return ;
        }catch (Exception e){
            System.out.println("第"+i+"此连接，"+(connectState ? "服务器已连接成功！":"服务器已连接失败！"));
            i++;
            try{
                Thread.sleep(2000);
            }catch (Exception ex){
                ex.printStackTrace();
            }
            getConnect();
        }
    }
    public static void main(String [] args){
        try{
            getConnect();
            Scanner sc = new Scanner(System.in);
            while (true){
                System.out.print("输入想要发送的内容: ");
                String input = sc.nextLine();
                // 发送请求
                ByteBuffer buffer = ByteBuffer.wrap(input.getBytes());
                socketChannel.write(buffer);
                // 读取响应
                ByteBuffer readBuffer = ByteBuffer.allocate(1024);
                int num;
                if ((num = socketChannel.read(readBuffer)) > 0) {
                    readBuffer.flip();
                    byte[] re = new byte[num];
                    readBuffer.get(re);
                    String result = new String(re, "UTF-8");
                    System.out.println("返回值: " + result);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
