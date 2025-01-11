package com.zhouyu.android_create.tools.theard;

import java.util.Date;
import java.util.concurrent.*;

public class PriorityExecutorService extends ThreadPoolExecutor {

    private final int queueLimit;

    public PriorityExecutorService(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, int queueLimit) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new PriorityBlockingQueue<Runnable>());
        this.queueLimit = queueLimit;
    }

    @Override
    public void execute(Runnable command) {
        PrioritizedTask task = new PrioritizedTask(command, System.currentTimeMillis());
        if (getQueue().size() >= queueLimit) {
            // 移除最旧的任务
            getQueue().poll();
        }
        super.execute(task);
    }


    public static class PrioritizedTask implements Runnable, Comparable<PrioritizedTask> {
        private final Runnable task;
        private final long priority; // 优先级，越小越优先
        private final Date submitTime;

        public PrioritizedTask(Runnable task, long priority) {
            this.task = task;
            this.priority = priority;
            this.submitTime = new Date();
        }

        @Override
        public void run() {
            task.run();
        }

        @Override
        public int compareTo(PrioritizedTask other) {
            return Long.compare(this.priority, other.priority);
        }

        public Date getSubmitTime() {
            return submitTime;
        }
    }
}
