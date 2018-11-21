package com.brocast.riak.api.beans;


import com.google.gson.GsonBuilder;

import java.util.Set;

/**
 * Created by sardor on 1/3/14.
 */

public class DcMediaEntity {

    public Long idMedia;
    public String title;
    public Integer duration;
    public String dateadded;
    public Integer idCategory = 1;
    public Long idUser;
    public String description;
    public Integer progress;
    public String location;
    public Long idChannel = 0l;
    public String thumb;
    public String thumb_webp;
    public String lang;
    public LiveProps liveProps;
    public String coordinate = "0,0";
    public Long lastUpdate;
    public Long rating;

    public Set<String> tags;
    public String ratio = "4:3";

    public String method = "upload"; //live, event, upload, recorded

    public Long getIdMedia() {
        return idMedia;
    }

    public void setIdMedia(Long idMedia) {
        this.idMedia = idMedia;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getDateadded() {
        return dateadded;
    }

    public void setDateadded(String dateadded) {
        this.dateadded = dateadded;
    }

    public Integer getIdCategory() {
        return idCategory;
    }

    public void setIdCategory(Integer idCategory) {
        this.idCategory = idCategory;
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getIdChannel() {
        return idChannel;
    }

    public void setIdChannel(Long idChannel) {
        this.idChannel = idChannel;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getThumb_webp() {
        return thumb_webp;
    }

    public void setThumb_webp(String thumb_webp) {
        this.thumb_webp = thumb_webp;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public LiveProps getLiveProps() {
        return liveProps;
    }

    public void setLiveProps(LiveProps liveProps) {
        this.liveProps = liveProps;
    }

    public String getCoordinate() {
        return coordinate;
    }

    public void setCoordinate(String coordinate) {
        this.coordinate = coordinate;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getRatio() {
        return ratio;
    }

    public void setRatio(String ratio) {
        this.ratio = ratio;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Long getRating() {
        return rating;
    }

    public void setRating(Long rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
