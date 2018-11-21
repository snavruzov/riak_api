package com.brocast.riak.api.beans;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2016.
 */
public class LiveProps {
    public String rtmp_url = "";
    public String hls_url = "";
    public String mp4_url = "";
    public String dash_url;
    public String rtc_url;
    public Boolean debate = false;
    public String position = ""; //sbs - side-by-side, pop - picture-on-picture

    public LiveProps() {
    }
}
