package com.brocast.riak.api.factory;

import com.brocast.riak.api.beans.*;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2016.
 */
public interface IRiakSaveFactory {
    Integer buildUserData(DcUsersEntity entity);
    void updUserNotificationSettings(String idUser, DcUserNotificationSettings settings, String type);
    void buildUserNotificationSettings(String idUser);
    void updConversation(DcRoomEntity room);
    void updConversationAck(DcRoomEntity room);
    void updRoomUserlist(DcRoomEntity room, boolean delUser);
    void storeInboxMessage(DcMessageEntity entity);
    Integer buildMediaContent(DcMediaEntity entity);
    void updUserData(DcUsersEntity entity);
    void storeMediaStatistics(Long idMedia, boolean init, String field);
    void removeRoom(String idAthor, String idInbx, String id_room);
    void updMediaContent(DcMediaEntity e);
    Integer buildChannelContent(DcChannelsEntity entity);
    void updChannelContent(DcChannelsEntity e);
    void removeMedia(Long idMedia);
    RoomUsersInfo bldConversation(String idAuthor, String idUsers);
    void bldComment(DcCommentsEntity comment);
    void updMessageData(DcMessageEntity e);
    void buildMediaComments(DcCommentsEntity entity);
    void updUserComment(DcCommentsEntity c);
    void removeMediaComments(Long idComment);

}

