package com.brocast.riak.api.beans;

import java.util.Set;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2016.
 */
public class DcUserNotificationSettings {
    public Boolean live;
    public Boolean promo;
    public Boolean follower;
    public Boolean channel;
    public Boolean comment;
    public Boolean inbox;
    public Set<String> exception;
}
