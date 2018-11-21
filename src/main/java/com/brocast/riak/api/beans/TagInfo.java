package com.brocast.riak.api.beans;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2017.
 */
public class TagInfo {
    public String tag;
    public long videonum;
    public long flwnum;
    public boolean reFollow;

    public TagInfo() {
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public long getVideonum() {
        return videonum;
    }

    public void setVideonum(long videonum) {
        this.videonum = videonum;
    }

    public long getFlwnum() {
        return flwnum;
    }

    public void setFlwnum(long flwnum) {
        this.flwnum = flwnum;
    }

    public boolean isReFollow() {
        return reFollow;
    }

    public void setReFollow(boolean reFollow) {
        this.reFollow = reFollow;
    }
}
