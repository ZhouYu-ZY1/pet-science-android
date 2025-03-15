package com.zhouyu.pet_science.tools.utils;

import java.util.concurrent.TimeUnit;

//限制请求次数
public class RateLimiter {
    private final int permits; // 请求数限制
    private final long refillInterval; // 令牌刷新时间间隔
    private long lastRefillTime; // 上次令牌刷新时间
    private int numPermits; // 当前可用令牌数


    public RateLimiter(int permits, TimeUnit timeUnit) {
        this.permits = permits;
        this.refillInterval = timeUnit.toNanos(1);
        this.numPermits = permits;
        this.lastRefillTime = System.nanoTime();
    }

    public synchronized boolean tryAcquire() {
        // 计算时间间隔
        long now = System.nanoTime();
        long timeSinceLastRefill = now - lastRefillTime;
        // 计算应该增加多少个令牌
        int newPermits = (int) (timeSinceLastRefill * permits / refillInterval);
        // 更新令牌数和上次刷新时间
        numPermits = Math.min(numPermits + newPermits, permits);
        lastRefillTime = now;
        // 检查是否有可用令牌
        if (numPermits > 0) {
            numPermits--;
            return true;
        } else {
            return false;
        }
    }
}