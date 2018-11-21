package com.brocast.riak.api.factory;

import com.brocast.riak.api.beans.*;

import java.util.List;
import java.util.Set;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2016.
 */
public interface IRiakQueryFactory {
    List<DcRoomEntity> queryRoomList(String key, String idUser);
    DcRoomEntity queryRoomByID(String key);
    DcUsersEntity queryUserDataByIDUser(Long idUser);
    DcUsersEntity queryUserDataByIDSocial(String idSocial, String type);
    DcUsersEntity queryUserDataByIDEmail(String email);
    List<DcMessageEntity> queryUserChatByIDRoom(String id_room, Integer off, Integer rows);
    List<DcMediaEntity> queryMediaDataByCoordinate(String latlong, Integer off, Integer rows);
    DcUserNotificationSettings queryUserNotificationSettings(Long idUser, String type);
    DcMediaEntity queryMediaDataByID(Long idMedia);
    DcChannelsEntity queryChannelDataByID(Long idChannel);
    void queryUserMediaCollection(Long idUser);
    Set<String> queryUserDataByName(String name, String method, Long idUser, Integer off, Integer limit);
    List<DcMediaEntity> queryRelatedVideos(Set<String> tags, Long idUser, Long idMedia);
    List<DcMediaEntity> queryMediaDataByTitle(String name, Integer off, Integer rows);
    List<DcMediaEntity> queryMediaDataByTags(String tag, Integer off, Integer rows);
    List<DcMediaEntity> queryMediaDataByPlace(String place, Integer off, Integer rows);
    Set<String> queryTagDataByName(String name, Integer off, Integer rows);
    List<DcChannelsEntity> queryChannelDataByTitle(String name, Integer off, Integer rows);
    List<DcChannelsEntity> queryChannelDataByIDuser(Long idUser, Integer off, Integer rows);
    List<DcChannelsEntity> queryChannelDataByFollows(Long idUser, Integer off, Integer rows);
    List<DcMediaEntity> queryUserVideoDataByIduser(Long idUser, int off, int rows);
    Long queryUsersChannelCountByIDUser(Long idUser);
    Long queryUsersChannelMediaCountByIDChannel(Long idChannel);
    Long queryUsersVideoCountByIDUser(Long idUser);
    List<DcMediaEntity> queryMediaDataByIdChannel(Long idChannel, int off, int rows);
    Set<String> queryTagDataByTOP(Integer rows);
    Set<String> queryUsersByTOP(Integer rows);
    List<DcMediaEntity> queryMediaByTOP(Integer offset, Integer rows);
    List<DcMediaEntity> queryMediaByFeaturedTOP(Integer rows);
    List<DcMediaEntity> queryMediaByLast(Integer off, Integer rows);
    List<DcUsersEntity> queryUsersByLast(Integer off, Integer rows);
    List<DcMediaEntity> queryMediaByMAP(Integer offset, Integer rows);
    DcCommentsEntity queryCommentByIDComment(Long idComment);
    List<DcChannelsEntity> queryChannelDataByLast(Integer off, Integer rows);
    List<DcChannelsEntity> queryChannelDataByTOP(Integer off, Integer rows);
    List<DcMediaEntity> queryUserMediaDataByEvent(Long idUser, Integer off, Integer rows);
    List<EventListEntity> queryUserEventList(Long idUser, Integer off, Integer rows);
    RoomUsersInfo queryRoomByIdRoom(String key);
    List<DcChannelsEntity> queryChannelDataByTitleByIDUser(String name, Long idUser, Integer off, Integer rows);
    List<DcChannelsEntity> queryChannelDataByTitleByFollowing(String name, Long idUser, Integer off, Integer rows);
    MediaStatistics queryMediaViewStatistics(Long idMedia);
    List<DcCommentsEntity> queryMediaComments(long duration, Long idMedia, int off, int rows, boolean reverse, boolean last);
    void queryMediaCollection(Long idUser);
}
