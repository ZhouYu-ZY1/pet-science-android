package com.zhouyu.android_create.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;

public class MusicPlayItemView extends RelativeLayout {
    public MusicPlayItemView(Context context) {
        super(context);
    }

    public MusicPlayItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MusicPlayItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public MusicPlayItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    private String musicId;

    private boolean isHighlightShowMusic;

    public String getMusicId() {
        return musicId;
    }

    public void setMusicId(String musicId) {
        this.musicId = musicId;
    }

    public boolean isHighlightShowMusic() {
        return isHighlightShowMusic;
    }

    public void setHighlightShowMusic(boolean highlightShowMusic) {
        isHighlightShowMusic = highlightShowMusic;
    }
}
