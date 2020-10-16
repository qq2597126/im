package com.lcy.common.concurrent;





import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class FutureTaskScheduler extends Thread {
    //private final Logger logger = Logger.getLogger(this.getClass());
    private ConcurrentLinkedQueue<ExecuteTask> executeTaskQueue =
            new ConcurrentLinkedQueue<ExecuteTask>();// 任务队列

    private long sleepTime = 200;// 线程休眠时间
    //核心线程数
    int corePoolSize =  Runtime.getRuntime().availableProcessors()*2;

    //线程池
    private ExecutorService pool = new ThreadPoolExecutor(corePoolSize,corePoolSize,0,TimeUnit.SECONDS,new  LinkedBlockingQueue());

    private static FutureTaskScheduler inst = new FutureTaskScheduler();

    private FutureTaskScheduler() {
        this.start();
    }

    /**
     * 添加任务
     *
     * @param executeTask
     */


    public static void add(ExecuteTask executeTask) {
        inst.executeTaskQueue.add(executeTask);
    }

    @Override
    public void run() {
        while (true) {
            handleTask();// 处理任务
            threadSleep(sleepTime);
        }
    }

    private void threadSleep(long time) {
        try {
            sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理任务队列，检查其中是否有任务
     */
    private void handleTask() {
        try {
            ExecuteTask executeTask = null;
            while ((executeTask = executeTaskQueue.poll()) != null) {
                handleTask(executeTask);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行任务操作
     *
     * @param executeTask
     */
    private void handleTask(ExecuteTask executeTask) {
        pool.execute(new ExecuteRunnable(executeTask));
    }

    class ExecuteRunnable implements Runnable {
        ExecuteTask executeTask;

        ExecuteRunnable(ExecuteTask executeTask) {
            this.executeTask = executeTask;
        }

        public void run() {
            executeTask.execute();
        }
    }
}
