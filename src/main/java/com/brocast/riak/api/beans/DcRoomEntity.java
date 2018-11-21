package com.brocast.riak.api.beans;

import java.util.Set;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2016.
 */
public class DcRoomEntity {

    public String id_inbx;
    public String id_room;
    public Set<String> userlist;
    public String idAuthor;
    public Long msg_count;
    public String msg_type = "1";
    public String last_msg;
    public String last_upd;
    public String avatar;
    public Boolean group;
    public String username;

    public DcRoomEntity() {
    }

    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getGroup() {
        return group;
    }

    public void setGroup(Boolean group) {
        this.group = group;
    }

    public Set<String> getUserlist() {
        return userlist;
    }

    public void setUserlist(Set<String> userlist) {
        this.userlist = userlist;
    }

    public String getId_room() {
        return id_room;
    }

    public void setId_room(String id_room) {
        this.id_room = id_room;
    }

    public String getId_inbx() {
        return id_inbx;
    }

    public void setId_inbx(String id_inbx) {
        this.id_inbx = id_inbx;
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

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "DcRoomEntity{" +
                "id_inbx='" + id_inbx + '\'' +
                ", id_room='" + id_room + '\'' +
                ", userlist=" + userlist +
                ", idAuthor='" + idAuthor + '\'' +
                ", msg_count=" + msg_count +
                ", msg_type='" + msg_type + '\'' +
                ", last_msg='" + last_msg + '\'' +
                ", last_upd='" + last_upd + '\'' +
                ", avatar='" + avatar + '\'' +
                ", group=" + group +
                ", username='" + username + '\'' +
                '}';
    }
}
