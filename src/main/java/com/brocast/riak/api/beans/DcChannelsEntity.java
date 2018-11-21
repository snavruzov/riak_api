package com.brocast.riak.api.beans;

import com.google.gson.GsonBuilder;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2016.
 */
public class DcChannelsEntity {

    public Long idChannel;
    public String title;
    public String description;
    public String avatar;
    public String wall="empty";
    public Long idUser;
    public Long mcount;
    public Long ucount;
    public Boolean enabled;
    public String dateadded;
    public Integer privacy; // 0 - public, 1 - private
    public short state;
    public ChannelProps props;
    public Boolean trending;

    public DcChannelsEntity() {
    }

    public Long getIdChannel() {
        return idChannel;
    }

    public void setIdChannel(Long idChannel) {
        this.idChannel = idChannel;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getWall() {
        return wall;
    }

    public void setWall(String wall) {
        this.wall = wall;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public Long getMcount() {
        return mcount;
    }

    public void setMcount(Long mcount) {
        this.mcount = mcount;
    }

    public Long getUcount() {
        return ucount;
    }

    public void setUcount(Long ucount) {
        this.ucount = ucount;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getDateadded() {
        return dateadded;
    }

    public void setDateadded(String dateadded) {
        this.dateadded = dateadded;
    }

    public Integer getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Integer privacy) {
        this.privacy = privacy;
    }

    public short getState() {
        return state;
    }

    public void setState(short state) {
        this.state = state;
    }

    public ChannelProps getProps() {
        return props;
    }

    public void setProps(ChannelProps props) {
        this.props = props;
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
