package com.brocast.riak.api.beans;

import java.util.Collections;
import java.util.Set;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2017.
 */
public class EventListEntity {
    public String title;
    public String idMedia;
    public Set<String> tags = Collections.emptySet();
    public String start_time;
    public String channelTitle;
    public Long idChannel = 0l;

    public EventListEntity() {
    }

    public EventListEntity(String title, String idMedia, Set<String> tags, String start_time, String channelTitle, Long idChannel) {
        this.title = title;
        this.idMedia = idMedia;
        this.tags = tags;
        this.start_time = start_time;
        this.channelTitle = channelTitle;
        this.idChannel = idChannel;
    }
}
