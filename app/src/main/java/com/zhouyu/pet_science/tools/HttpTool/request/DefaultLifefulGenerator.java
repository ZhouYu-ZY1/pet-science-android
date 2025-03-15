package com.zhouyu.pet_science.tools.HttpTool.request;

import java.lang.ref.WeakReference;

/**
 * 默认生命周期管理包装生成器。
 */

public class DefaultLifefulGenerator<Callback> implements LifefulGenerator<Callback> {

    private WeakReference<Lifeful> mLifefulWeakReference;
    private boolean mLifefulIsNull;
    private Callback mCallback;

    public DefaultLifefulGenerator(Callback callback, Lifeful lifeful) {
        mCallback = callback;
        mLifefulWeakReference = new WeakReference<>(lifeful);
        mLifefulIsNull = lifeful == null;
    }

    @Override
    public Callback getCallback() {
        return mCallback;
    }

    public WeakReference<Lifeful> getLifefulWeakReference() {
        return mLifefulWeakReference;
    }

    @Override
    public boolean isLifefulNull() {
        return mLifefulIsNull;
    }
}
