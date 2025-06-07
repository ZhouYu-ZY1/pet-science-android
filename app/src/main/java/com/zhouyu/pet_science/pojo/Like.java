package com.zhouyu.pet_science.pojo;

public class Like {
    private Video.Data content;
    private boolean isLike;

    public Like(Video.Data video, boolean isLike) {
        this.content = video;
        this.isLike = isLike;
    }
}
