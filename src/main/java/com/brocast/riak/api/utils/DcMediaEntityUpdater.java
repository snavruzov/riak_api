package com.brocast.riak.api.utils;

import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.brocast.riak.api.beans.DcMediaEntity;
import com.dgtz.mcache.api.factory.Constants;
import com.dgtz.mcache.api.factory.RMemoryAPI;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2016.
 */
public class DcMediaEntityUpdater extends UpdateValue.Update<DcMediaEntity> {
    private DcMediaEntity e;

    public DcMediaEntityUpdater(DcMediaEntity e) {
        this.e = e;
    }

    @Override
    public DcMediaEntity apply(DcMediaEntity entity) {
        entity.description = e.description;
        entity.lastUpdate = System.currentTimeMillis();
        RMemoryAPI.getInstance()
                .pushHashToMemory(Constants.MEDIA_KEY + entity.idMedia, "last_update", entity.lastUpdate+"");
        entity.duration = e.duration;
        entity.idChannel = e.idChannel;
        entity.liveProps = e.liveProps;
        entity.title = e.title;
        entity.tags = e.tags;
        entity.ratio = e.ratio;
        entity.progress = e.progress;
        entity.method = e.method;
        entity.location = e.location;
        entity.rating = e.rating;

        return entity;
    }
}
