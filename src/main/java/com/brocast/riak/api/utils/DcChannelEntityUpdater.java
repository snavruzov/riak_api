package com.brocast.riak.api.utils;

import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.brocast.riak.api.beans.ChannelProps;
import com.brocast.riak.api.beans.DcChannelsEntity;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2016.
 */
public class DcChannelEntityUpdater extends UpdateValue.Update<DcChannelsEntity> {

    private DcChannelsEntity ch;
    public DcChannelEntityUpdater(DcChannelsEntity channel) {
        this.ch = channel;
    }

    @Override
    public DcChannelsEntity apply(DcChannelsEntity dcChannelsEntity) {
        ChannelProps props = new ChannelProps();
        props.access = dcChannelsEntity.props.access;

        dcChannelsEntity.avatar = ch.avatar;
        dcChannelsEntity.props = props;
        dcChannelsEntity.description = ch.description;
        dcChannelsEntity.enabled = ch.enabled;
        dcChannelsEntity.mcount = ch.mcount;
        dcChannelsEntity.privacy = ch.privacy;
        dcChannelsEntity.title = ch.title;
        dcChannelsEntity.ucount = ch.ucount;
        dcChannelsEntity.wall = ch.wall;

        return dcChannelsEntity;
    }
}
