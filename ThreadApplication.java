package com.jianyun.thread.jianyun;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.*;

/**
 * @Auther: chenkui
 * @Date: 2019/2/13 14:06
 * @Description: 实现子线程计算汇总，另外可参考fork/join的实现方式
 */
public class ThreadApplication {
    private static Stack<Integer> stack = new Stack();
    public static ExecutorService getExcutorPool(){
        //ExecutorService executorService = Executors.newCachedThreadPool();
        //核心线程数、最大线程数、空闲线程等待时长后自动关闭、时间单位、任务队列、拒绝策略处理
        ThreadPoolExecutor executorService =  new ThreadPoolExecutor(10, 20, 20, TimeUnit.SECONDS, new LinkedBlockingQueue<>(100), new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                if (!executor.isShutdown()){
                    executor.shutdown();
                }
            }
        });
        return executorService;
    }
    public static void main(String[] args){
        Map<Integer,Integer> map = new HashMap();
        map.put(1,2);
        map.put(3,4);
        map.put(5,6);
        map.put(7,8);
        CountDownLatch latch = new CountDownLatch(map.size());
        for (Integer key : map.keySet()){
            Integer value = map.get(key);
            FutureTask<Integer> task = new FutureTask(new ThreadApplication(). new ComputerCall(key, value));
            try{
                getExcutorPool().submit(task);
                latch.countDown();
                //等待超时设置
                if (task.get(2,TimeUnit.SECONDS)!=null){
                    stack.push(task.get());
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        //整体等待时长
        try{
            latch.await(2*map.size(),TimeUnit.SECONDS);
        }catch (Exception e){
            e.printStackTrace();
        }
        Long totalResult = 0l;
        for (Integer itemResult : stack){
            totalResult += itemResult;
        }
        System.out.println(Thread.currentThread().getName()+"总计结果totalResult："+totalResult);
    }
    class ComputerCall implements Callable<Integer>{
        private Integer key;
        private Integer value;
        ComputerCall(final Integer key,final Integer value){
            this.key = key;
            this.value =value;
        }
        @Override
        public Integer call() {
            System.out.println(Thread.currentThread().getId()+"-----"+Thread.currentThread().getName()+"本次计算"+key+"+"+value+"结果："+(key+value));
            return key+value;
        }
    }
}
