package com.zhouyu.pet_science.pojo;

public class Like {
    private Video.Data video;
    private boolean isLike;

    public Like(Video.Data video, boolean isLike) {
        this.video = video;
        this.isLike = isLike;
    }
}
