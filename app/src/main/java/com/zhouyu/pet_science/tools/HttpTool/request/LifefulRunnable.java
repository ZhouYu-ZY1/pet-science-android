package com.zhouyu.pet_science.tools.HttpTool.request;

/**
 * 与周期相关的异步线程回调类。
 */
public class LifefulRunnable implements Runnable {

    private final LifefulGenerator<Runnable> mLifefulGenerator;

    public LifefulRunnable(Runnable runnable, Lifeful lifeful) {
        mLifefulGenerator = new DefaultLifefulGenerator<>(runnable, lifeful);
    }

    @Override
    public void run() {
        if (LifefulUtils.shouldGoHome(mLifefulGenerator)) {
            return;
        }
        mLifefulGenerator.getCallback().run();
    }
}
