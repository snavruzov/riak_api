package com.brocast.riak.api.beans;

import java.util.List;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2017.
 */
public class RoomUsersInfo {
    public List<RoomShortInfo> data;
    public String id_room;
    public String idAuthor;
    public Long msg_count;
    public String last_msg;
    public String msg_type = "1";
    public String last_upd;

    public RoomUsersInfo() {
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public List<RoomShortInfo> getData() {
        return data;
    }

    public void setData(List<RoomShortInfo> data) {
        this.data = data;
    }

    public String getId_room() {
        return id_room;
    }

    public void setId_room(String id_room) {
        this.id_room = id_room;
    }

    public String getIdAuthor() {
        return idAuthor;
    }

    public void setIdAuthor(String idAuthor) {
        this.idAuthor = idAuthor;
    }

    public Long getMsg_count() {
        return msg_count;
    }

    public void setMsg_count(Long msg_count) {
        this.msg_count = msg_count;
    }

    public String getLast_msg() {
        return last_msg;
    }

    public void setLast_msg(String last_msg) {
        this.last_msg = last_msg;
    }

    public String getLast_upd() {
        return last_upd;
    }

    public void setLast_upd(String last_upd) {
        this.last_upd = last_upd;
    }
}
