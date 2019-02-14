package com.jianyun.thread.jianyun;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @Auther: chenkui
 * @Date: 2019/2/14 17:55
 * @Description:关于多线程中自旋锁的使用(配合Future设置超时时间，以防止死锁)
 */
public class Spinlock {
    private static final AtomicReference<Thread> atomicReference =  new AtomicReference();
    public void lock(){
        Thread current = Thread.currentThread();
        while (!atomicReference.compareAndSet(null, current)){
            System.out.println(Thread.currentThread().getId()+Thread.currentThread().getName()+"处于自旋状态");
        }
    }
    public void unlock(){
        Thread current = Thread.currentThread();
        atomicReference .compareAndSet(current, null);
        System.out.println(Thread.currentThread().getId()+Thread.currentThread().getName()+"结束自旋");
    }
    public void computer(int x ,int y){
        System.out.println(Thread.currentThread().getId()+Thread.currentThread().getName()+"计算结果："+(x+y));
    }
    public static void main(String[] args){
        Spinlock spinlock= new Spinlock();//同一资源的竞争
        new Thread(new Runnable() {
            @Override
            public void run() {
                spinlock.lock();
                spinlock.computer(1,2);
                try{
                    Thread.sleep(2000);
                    spinlock.unlock();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                spinlock.lock();
                spinlock.computer(3,4);
                spinlock.unlock();
            }
        }).start();
    }
}
