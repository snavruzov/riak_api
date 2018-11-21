package com.brocast.riak.api.beans;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2017.
 */
public class RoomShortInfo {
    public String id_inbx;
    public String idUser;
    public String avatar;
    public String username;
    public long msg_count;

    public long getMsg_count() {
        return msg_count;
    }

    public void setMsg_count(long msg_count) {
        this.msg_count = msg_count;
    }

    public String getId_inbx() {
        return id_inbx;
    }

    public void setId_inbx(String id_inbx) {
        this.id_inbx = id_inbx;
    }

    public String getIdUser() {
        return idUser;
    }

    public void setIdUser(String idUser) {
        this.idUser = idUser;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
