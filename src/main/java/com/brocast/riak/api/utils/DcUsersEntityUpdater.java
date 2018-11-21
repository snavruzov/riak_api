package com.brocast.riak.api.utils;

import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.brocast.riak.api.beans.DcUsersEntity;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2016.
 */
public class DcUsersEntityUpdater extends UpdateValue.Update<DcUsersEntity> {
    private DcUsersEntity e;

    public DcUsersEntityUpdater(DcUsersEntity user){
        e = user;
    }

    @Override
    public DcUsersEntity apply(DcUsersEntity entity) {
        entity.about = e.about;
        entity.avatar = e.avatar;
        entity.city = e.city;
        entity.country = e.country;
        entity.email = e.email;
        entity.fullname = e.fullname;
        entity.hash = e.hash;
        entity.username = e.username;
        entity.idFBSocial = e.idFBSocial;
        entity.idGSocial = e.idGSocial;
        entity.idTWTRSocial = e.idTWTRSocial;
        entity.idVKSocial = e.idVKSocial;
        entity.lang = e.lang;
        entity.secword = e.secword;
        entity.social_links = e.social_links;
        entity.wallpic = e.wallpic;
        entity.verified = e.verified;
        entity.type = e.type;
        entity.stars = e.stars;
        entity.enabled = 1;

        return entity;
    }
}
