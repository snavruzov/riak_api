package com.brocast.riak.api.utils;

import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.brocast.riak.api.beans.DcMessageEntity;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2017.
 */
public class DcMessageEntityUpdater extends UpdateValue.Update<DcMessageEntity>{

    private DcMessageEntity msg;
    public DcMessageEntityUpdater(DcMessageEntity message) {
        this.msg = message;
    }

    @Override
    public DcMessageEntity apply(DcMessageEntity messageEntity) {

        //messageEntity.url = msg.url;
        //messageEntity.body = msg.body;
        messageEntity.is_read = msg.is_read;
        //messageEntity.is_deleted = msg.is_deleted;

        return messageEntity;
    }

}
