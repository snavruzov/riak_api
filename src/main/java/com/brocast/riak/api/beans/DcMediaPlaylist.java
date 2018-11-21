package com.brocast.riak.api.beans;

import com.google.gson.GsonBuilder;

/**
 * Created by sardor on 4/2/17.
 */
public class DcMediaPlaylist {
    public String id_playlist;
    public Long idUser;
    public String title;
    public String username;
    public long vcount;
    public String descr;

    public DcMediaPlaylist() {
    }

    public String getId_playlist() {
        return id_playlist;
    }

    public void setId_playlist(String id_playlist) {
        this.id_playlist = id_playlist;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescr() {
        return descr;
    }

    public void setDescr(String descr) {
        this.descr = descr;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getVcount() {
        return vcount;
    }

    public void setVcount(long vcount) {
        this.vcount = vcount;
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
