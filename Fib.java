package com.jianyun.thread.jianyun;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;

/**
 * @Auther: chenkui
 * @Date: 2019/2/14 10:36
 * @Description:计算斐波那契数据前N项的和(n>1)
 */
public class Fib extends RecursiveTask<Integer> {
    private static final int threshold = 5;//分组的长度
    private Integer number;
    private Integer start;
    private Integer end;

    Fib(int n) {
        number = n;
    }
    private Fib(int start, int end){
        this.start = start;
        this.end = end;
    }

    @Override
    protected  Integer compute() {
        return run();
    }

    public int run() {
        int total = 0;
        if (number!=null){
            //分组处理,包含n组threshold
            int n = number/threshold;
            if (n==0){
                for(int i=0;i<=number;i++){
                    total += seqFib(i);
                }
            }else if(n>0){
                Fib item = null;
                for (int i=0;i<n;i++){
                    int start = i*threshold+1;
                    int end = (i+1)*threshold;
                    item = new Fib(start, end);
                    item.fork();
                    total = total+item.join();
                }
                if (n*threshold<number){
                    int start = n*threshold+1;
                    int end = n*threshold+(number%threshold);
                    for(int i=start; i<=end;i++){
                        total += seqFib(i);
                    }
                }

            }
        }else if (start!=null && end!=null){
            for(int i =start;i<=end;i++){
                total += seqFib(i);
            }
        }
        return total;
    }

    int seqFib(int n) {
        if (n <= 1) return n;
        else return seqFib(n- 1) + seqFib(n-2);
    }
    public static void main(String[] args) {
        try {
            int n =20;
            ForkJoinPool forkJoinPool = new ForkJoinPool();
            Fib f = new Fib(n);
            Future<Integer> future =forkJoinPool.submit(f);
            int result = future.get();
            System.out.println("前"+n+"项和的结果result=" + result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
