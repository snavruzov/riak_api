package com.brocast.riak.api.beans;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2016.
 */
public class DcMessageEntity {

    public Long idUser = 0l;
    public Long msg_type; //1-text, 2-share, 3-video, 4-picture, 5-voice, 6-system, 7-channel, 8-debate
    public String username = "";
    public String avatar = "";
    public String dateadded = System.currentTimeMillis()+"";
    public String url = "";
    public String body = "";
    public Boolean is_read;
    public String id_room;
    public String id_msg;
    public Boolean is_deleted;
    public int duration = 0;
    public Long idMedia = 0l;
    public Long idChannel = 0l;
    public String status = "valid";
    public String author_name = "";
    public Long idAuthor = 0l;
    public String method = "recorded";
    public String thumb = "";


    @Override
    public String toString() {
        return "DcMessageEntity{" +
                "idUser=" + idUser +
                ", msg_type=" + msg_type +
                ", username='" + username + '\'' +
                ", avatar='" + avatar + '\'' +
                ", dateadded='" + dateadded + '\'' +
                ", url='" + url + '\'' +
                ", body='" + body + '\'' +
                ", is_read=" + is_read +
                ", id_room='" + id_room + '\'' +
                ", id_msg='" + id_msg + '\'' +
                ", is_deleted=" + is_deleted +
                ", duration=" + duration +
                ", idMedia=" + idMedia +
                ", idChannel=" + idChannel +
                ", status='" + status + '\'' +
                ", author_name='" + author_name + '\'' +
                ", idAuthor='" + idAuthor + '\'' +
                ", method='" + method + '\'' +
                ", thumb='" + thumb + '\'' +
                '}';
    }
}
