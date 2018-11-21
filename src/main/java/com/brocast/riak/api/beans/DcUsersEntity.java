package com.brocast.riak.api.beans;


import com.google.gson.GsonBuilder;

import java.util.Set;

/**
 * Created by sardor on 1/3/14.
 */

public class DcUsersEntity {

    public Long idUser;
    public String username;
    public String fullname;
    public String secword ="";
    public String email = "";
    public String date_reg;
    public PrivateInfo profile;
    public Integer enabled;
    public String avatar;
    public String wallpic;
    public String hash;
    public String idFBSocial = "";
    public Set<String> social_links;
    public String idGSocial = "";
    public String idVKSocial = "";
    public String idTWTRSocial = "";
    public String city = "";
    public String country ="";
    public String about ="";
    public Boolean verified = false;
    public String lang = "en";
    public String idInbox;
    public Integer type = 0; // 0-basic, 1-profi
    public Long stars = 0l;

    public DcUsersEntity() {
    }

    public Long getIdUser() {
        return idUser;
    }

    public void setIdUser(Long idUser) {
        this.idUser = idUser;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getSecword() {
        return secword;
    }

    public void setSecword(String secword) {
        this.secword = secword;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDate_reg() {
        return date_reg;
    }

    public void setDate_reg(String date_reg) {
        this.date_reg = date_reg;
    }

    public PrivateInfo getProfile() {
        return profile;
    }

    public void setProfile(PrivateInfo profile) {
        this.profile = profile;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getWallpic() {
        return wallpic;
    }

    public void setWallpic(String wallpic) {
        this.wallpic = wallpic;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getIdFBSocial() {
        return idFBSocial;
    }

    public void setIdFBSocial(String idFBSocial) {
        this.idFBSocial = idFBSocial;
    }

    public Set<String> getSocial_links() {
        return social_links;
    }

    public void setSocial_links(Set<String> social_links) {
        this.social_links = social_links;
    }

    public String getIdGSocial() {
        return idGSocial;
    }

    public void setIdGSocial(String idGSocial) {
        this.idGSocial = idGSocial;
    }

    public String getIdVKSocial() {
        return idVKSocial;
    }

    public void setIdVKSocial(String idVKSocial) {
        this.idVKSocial = idVKSocial;
    }

    public String getIdTWTRSocial() {
        return idTWTRSocial;
    }

    public void setIdTWTRSocial(String idTWTRSocial) {
        this.idTWTRSocial = idTWTRSocial;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getIdInbox() {
        return idInbox;
    }

    public void setIdInbox(String idInbox) {
        this.idInbox = idInbox;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return new GsonBuilder().create().toJson(this);
    }
}
