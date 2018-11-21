package com.brocast.riak.api.beans;


import com.google.gson.GsonBuilder;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: Sardor Navruzov
 * Date: 12/21/13
 * Time: 9:30 PM
 */

public class DcCommentsEntity implements Serializable{

    private static final long serialVersionUID = 1L;

    public long idComment;
    public String dateadded;
    public String username;
    public String avatar;
    public Long tkey;
    public Long idUser;
    public String text;
    public Long idMedia;
    public String url = "";
    public int commentType=0; // 0 - common, 1 - voice, 2-system
    public int vPermit=0; // 0 - deny, 1 - allow
    public long duration=0;

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getCommentType() {
        return commentType;
    }

    public void setCommentType(int commentType) {
        this.commentType = commentType;
    }

    public int getvPermit() {
        return vPermit;
    }

    public void setvPermit(int vPermit) {
        this.vPermit = vPermit;
    }

    public Long getTkey() {
        return tkey;
    }

    public void setTkey(Long tkey) {
        this.tkey = tkey;
    }

    public long getIdComment() {
        return idComment;
    }

    public void setIdComment(long idComment) {
        this.idComment = idComment;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getIdMedia() {
        return idMedia;
    }

    public void setIdMedia(Long idMedia) {
        this.idMedia = idMedia;
    }

    public String getDateadded() {
        return dateadded;
    }

    public void setDateadded(String dateadded) {
        this.dateadded = dateadded;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }


    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
