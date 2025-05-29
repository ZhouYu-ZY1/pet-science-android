package com.zhouyu.pet_science.pojo;

import java.util.List;

public class Video {
    private Integer code;
    private List<Data> data;
    private String message;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public List<Data> getData() {
        return data;
    }

    public void setData(List<Data> data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public static class Data {
        private String authorAvatar;
        private String coverSrc;
        private String desc;
        private String nickname;
        private String shareUrl;
        private String uid;
        private String videoId;
        private String videoSrc;
        private String type;
        private boolean like;

        public String getAuthorAvatar() {
            return authorAvatar;
        }

        public void setAuthorAvatar(String authorAvatar) {
            this.authorAvatar = authorAvatar;
        }

        public String getCoverSrc() {
            return coverSrc;
        }

        public void setCoverSrc(String coverSrc) {
            this.coverSrc = coverSrc;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public String getShareUrl() {
            return shareUrl;
        }

        public void setShareUrl(String shareUrl) {
            this.shareUrl = shareUrl;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getVideoId() {
            return videoId;
        }

        public void setVideoId(String videoId) {
            this.videoId = videoId;
        }

        public String getVideoSrc() {
            return videoSrc;
        }

        public void setVideoSrc(String videoSrc) {
            this.videoSrc = videoSrc;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isLike() {
            return like;
        }

        public void setLike(boolean like) {
            this.like = like;
        }

        @Override
        public String toString() {
            return "Data{" +
                    "authorAvatar='" + authorAvatar + '\'' +
                    ", coverSrc='" + coverSrc + '\'' +
                    ", desc='" + desc + '\'' +
                    ", nickname='" + nickname + '\'' +
                    ", shareUrl='" + shareUrl + '\'' +
                    ", uid='" + uid + '\'' +
                    ", videoId='" + videoId + '\'' +
                    ", videoSrc='" + videoSrc + '\'' +
                    ", type='" + type + '\'' +
                    ", like=" + like +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "Video{" +
                "code=" + code +
                ", data=" + data +
                ", message='" + message + '\'' +
                '}';
    }
}
