package com.brocast.riak.api.utils;

import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.brocast.riak.api.beans.DcCommentsEntity;

/**
 * Created by sardor on 5/24/17.
 */
public class DcCommentsEntityUpdater extends UpdateValue.Update<DcCommentsEntity>{
    private DcCommentsEntity e;

    public DcCommentsEntityUpdater(DcCommentsEntity comment) {
        this.e = comment;
    }

    @Override
    public DcCommentsEntity apply(DcCommentsEntity entity){
        entity.avatar = e.avatar;
        entity.commentType = e.commentType;
        entity.dateadded = e.dateadded;
        entity.duration = e.duration;
        entity.idComment = e.idComment;
        entity.idMedia = e.idMedia;
        entity.idUser = e.idUser;
        entity.text = e.text;
        entity.tkey = e.tkey;
        entity.url = e.url;
        entity.username = e.username;
        entity.vPermit = e.vPermit;

        return entity;
    }
}
