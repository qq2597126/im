package com.lcy.common.concurrent;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author lcy
 * @DESC:
 * @date 2020/9/27.
 */
public class ScheduledTask {


    private static ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()*3);


    //定时任务处理 按照上一次任务的发起时间计算下一次任务的开始时间
    public static void addScheduleAtFixedRate(ExecuteTask executeTask,long delay,long period,TimeUnit unit){
        scheduledExecutorService.scheduleAtFixedRate(new ScheduledRunnable(executeTask),delay,period,unit);
    }
    public static void addScheduleAtFixedRate(ExecuteTask executeTask,long period,TimeUnit unit){
        scheduledExecutorService.scheduleAtFixedRate(new ScheduledRunnable(executeTask),0,period,unit);
    }
    //定时任务处理 按照上一次任务的结束时间计算下一次任务的开始时间
    public static void scheduleWithFixedDelay(ExecuteTask executeTask,long delay,long period,TimeUnit unit){
        scheduledExecutorService.scheduleWithFixedDelay(new ScheduledRunnable(executeTask),delay,period,unit);
    }

    public static void scheduleWithFixedDelay(ExecuteTask executeTask,long period,TimeUnit unit){
        scheduledExecutorService.scheduleWithFixedDelay(new ScheduledRunnable(executeTask),0,period,unit);
    }
    public static void schedule(ExecuteTask executeTask,long delay,TimeUnit unit){
        scheduledExecutorService.schedule(new ScheduledRunnable(executeTask),delay,unit);
    }

    private static class ScheduledRunnable implements Runnable{
        private ExecuteTask executeTask;

        public ScheduledRunnable(ExecuteTask executeTask) {
            this.executeTask = executeTask;
        }

        @Override
        public void run() {
            try {
                executeTask.execute();
            }catch (Exception e){
                //异常处理
                executeTask.onException(e);
            }
        }
    }
}