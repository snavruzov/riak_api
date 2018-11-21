package com.brocast.riak.api.factory;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.datatypes.*;
import com.basho.riak.client.api.commands.kv.DeleteValue;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.api.commands.kv.UpdateValue;
import com.basho.riak.client.api.commands.search.Search;
import com.basho.riak.client.core.operations.SearchOperation;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.crdt.types.RiakMap;
import com.basho.riak.client.core.util.BinaryValue;
import com.brocast.riak.api.beans.*;
import com.brocast.riak.api.dao.RiakAPI;
import com.brocast.riak.api.dao.RiakTP;
import com.brocast.riak.api.utils.*;
import com.dgtz.mcache.api.factory.Constants;
import com.dgtz.mcache.api.factory.RMemoryAPI;
import com.google.gson.GsonBuilder;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2016.
 */
public class RiakSaveFactory implements IRiakSaveFactory{
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RiakSaveFactory.class);
    private RiakTP transport = null;
    private RiakClient client = null;

    public RiakSaveFactory(RiakTP transport) {
        this.transport = transport;
        this.client = this.transport.client;
    }

    @Override
    public Integer buildUserData(DcUsersEntity entity) {
        Integer updated = 0;
        try {
            log.debug("Saving user data into Riak {}", entity.toString());
            Location key = new Location(new Namespace("dc_users"), "dc_users:" + entity.idUser);

            entity.idInbox = MD5.hash(entity.idUser + "");
            StoreValue sv = new StoreValue.Builder(entity)
                    .withLocation(key)
                    .build();
            client.execute(sv);
            //updated = svResponse.getNumberOfValues();

            log.debug("getNumberOfValues {}", updated);

            String idRoom = initSystemConversation(client, entity.idUser + "");
            buildInbox(entity.idInbox, entity.idUser + "", idRoom, false);

        } catch (Exception e) {
            log.error("Error while updating user buildUserData()", e);
        }
        return updated;
    }

    @Override
    public void updUserNotificationSettings(String idUser, DcUserNotificationSettings settings, String type) {
        try {
            if (!type.equals("push") && !type.equals("email")) {
                throw new Exception();
            }
            SetUpdate ulist = new SetUpdate();
            settings.exception.forEach(ulist::add);
            DcNotificationTypes notificationTypes = new DcNotificationTypes();
            if(type.equals("email")){
                notificationTypes.email = settings;
            } else {
                notificationTypes.push = settings;
            }

            String jsonNSettings = new GsonBuilder().create().toJson(notificationTypes);
            RMemoryAPI.getInstance().pushHashToMemory(Constants.USER_KEY + idUser, "n_settings", jsonNSettings);

            Namespace usersBucket = new Namespace("user_nsettings", type);
            Location key = new Location(usersBucket, "dc_users:" + idUser);
            MapUpdate mu = new MapUpdate()
                    .update("live", new FlagUpdate(settings.live))
                    .update("promo", new FlagUpdate(settings.promo))
                    .update("follower", new FlagUpdate(settings.follower))
                    .update("comment", new FlagUpdate(settings.comment))
                    .update("exception", ulist)
                    .update("channel", new FlagUpdate(settings.channel))
                    .update("inbox", new FlagUpdate(settings.inbox));

            UpdateMap update = new UpdateMap.Builder(key, mu).build();
            client.execute(update);
        } catch (Exception e) {
            log.error("Error while updating user updUserNotificationSettings()", e);
        }
    }

    @Override
    public void buildUserNotificationSettings(String idUser) {

        try {

            DcUserNotificationSettings nsettings = new DcUserNotificationSettings();
            nsettings.channel = true;
            nsettings.comment = true;
            nsettings.exception = Collections.emptySet();
            nsettings.follower = true;
            nsettings.live = true;
            nsettings.promo = true;
            nsettings.inbox = true;
            DcNotificationTypes notificationTypes = new DcNotificationTypes();
            notificationTypes.email = nsettings;
            notificationTypes.push = nsettings;

            String jsonNSettings = new GsonBuilder().create().toJson(notificationTypes);
            RMemoryAPI.getInstance().pushHashToMemory(Constants.USER_KEY + idUser, "n_settings", jsonNSettings);

            Namespace usersBucket = new Namespace("user_nsettings", "push");
            Location key = new Location(usersBucket, "dc_users:" + idUser);
            MapUpdate mu = new MapUpdate()
                    .update("live", new FlagUpdate(true))
                    .update("promo", new FlagUpdate(true))
                    .update("follower", new FlagUpdate(true))
                    .update("comment", new FlagUpdate(true))
                    .update("exception", new SetUpdate())
                    .update("inbox", new FlagUpdate(true))
                    .update("channel", new FlagUpdate(true));

            UpdateMap update = new UpdateMap.Builder(key, mu).build();
            client.execute(update);

            usersBucket = new Namespace("user_nsettings", "email");
            key = new Location(usersBucket, "dc_users:" + idUser);
            mu = new MapUpdate()
                    .update("live", new FlagUpdate(true))
                    .update("promo", new FlagUpdate(true))
                    .update("follower", new FlagUpdate(true))
                    .update("comment", new FlagUpdate(true))
                    .update("exception", new SetUpdate())
                    .update("inbox", new FlagUpdate(true))
                    .update("channel", new FlagUpdate(true));

            update = new UpdateMap.Builder(key, mu).build();
            client.execute(update);
        } catch (Exception e) {
            log.error("Error while updating user default buildUserNotificationSettings()", e);
        }
    }

    private void buildInbox(String idInbox, String idUser, String id_room, boolean delRoom) throws Exception {

        RMemoryAPI.getInstance().pushHashToMemory(Constants.USER_KEY + idUser, "inbox", idInbox);
        if (delRoom) {
            RMemoryAPI.getInstance().delFromSetElem(Constants.USER_KEY + "inbox:room:" + idInbox, id_room);
        } else {
            RMemoryAPI.getInstance().pushSetElemToMemory(Constants.USER_KEY + "inbox:room:" + idInbox, id_room);
        }

    }

    private String initSystemConversation(RiakClient client, String idUser) throws Exception {

        Namespace bucket = new Namespace("inbox", "dc_room");
        MapUpdate mu = new MapUpdate()
                .update("userlist", new SetUpdate().add(idUser).add("1"))
                .update("group", new FlagUpdate(false))
                .update("id_author", new RegisterUpdate(idUser))
                .update("last_msg", new RegisterUpdate("Welcome to BroCast Live stream"))
                .update("last_upd", new RegisterUpdate(System.currentTimeMillis() + ""));
        UpdateMap update = new UpdateMap.Builder(bucket, mu)
                .build();
        String key = client.execute(update).getGeneratedKey().toString();
        RMemoryAPI.getInstance().pushSetElemToMemory(Constants.USER_KEY+"room:users:"+key, Formulas.SYSTEM_IDUSER);
        RMemoryAPI.getInstance().pushSetElemToMemory(Constants.USER_KEY+"room:users:"+key, idUser);
        RMemoryAPI.getInstance().pushHashToMemory(Constants.USER_KEY + idUser, "sysroom", key);
        log.debug("initSystemConversation key value {}", key);
        return key;
    }

    @Override
    public void updConversation(DcRoomEntity room) {
        try {
            Namespace bucket = new Namespace("inbox", "dc_room");
            Location location = new Location(bucket, room.id_room);

            MapUpdate mu = new MapUpdate()
                    .update("id_author", new RegisterUpdate(room.idAuthor))
                    .update("last_msg", new RegisterUpdate(room.last_msg))
                    .update("msg_type", new RegisterUpdate(room.msg_type))
                    .update("last_upd", new RegisterUpdate(System.currentTimeMillis() + ""));
            UpdateMap update = new UpdateMap.Builder(location, mu)
                    .build();
            client.execute(update);
        } catch (Exception e) {
            log.error("Error while updating user updConversation()", e);
        }
    }

    @Override
    public void updConversationAck(DcRoomEntity room) {
        try {
            MessageAckUtils ackUtils = new MessageAckUtils();
            ackUtils.updateMessageReadStatus(room.id_room, room.idAuthor);

            Namespace bucket = new Namespace("inbox", "dc_room");
            Location location = new Location(bucket, room.id_room);

            MapUpdate mu = new MapUpdate()
                    .update("last_upd", new RegisterUpdate(System.currentTimeMillis() + ""));
            UpdateMap update = new UpdateMap.Builder(location, mu)
                    .build();
            client.execute(update);
        } catch (Exception e) {
            log.error("Error while updating user updConversationAck()", e);
        }
    }

    @Override
    public void updUserData(DcUsersEntity u) {
        try {
            Location key = new Location(new Namespace("dc_users"), "dc_users:" + u.idUser);
            UpdateValue updateOp = new UpdateValue.Builder(key)
                    .withFetchOption(FetchValue.Option.DELETED_VCLOCK, true)
                    .withUpdate(new DcUsersEntityUpdater(u))
                    .build();
            client.execute(updateOp);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void updUserComment(DcCommentsEntity c) {
        try {
            Location key = new Location(new Namespace("dc_commentaries"), "dc_comment:" + c.idComment);
            UpdateValue updateOp = new UpdateValue.Builder(key)
                    .withFetchOption(FetchValue.Option.DELETED_VCLOCK, true)
                    .withUpdate(new DcCommentsEntityUpdater(c))
                    .build();
            client.execute(updateOp);
        } catch (Exception e){
            log.error("Error in update comment", e);
        }
    }

    private void delRoomMessageByIDUsers(final RiakClient client, DcRoomEntity room) throws Exception {
        StringBuilder query = new StringBuilder();
        query.append("idUser:(");
        for(String idu:room.getUserlist()){
            query.append(idu);
            query.append(" ");
        }
        query.delete(query.lastIndexOf(" "),query.length());
        query.append(") AND id_room:").append(room.id_room);

        Search searchOp = new Search
                .Builder("inbox", query.toString())
                .build();
        SearchOperation.Response res = client.execute(searchOp);

        List<Map<String, List<String>>> results = res.getAllResults();
        log.info("Getting search result of inbox msgs: {}", results.toString());

        results.forEach(doc -> {
            String bucket = doc.get("_yz_rb").get(0);
            String key = doc.get("_yz_rk").get(0);
            try {
                Location objectLocation = new Location(new Namespace(bucket), key);
                DeleteValue delete = new DeleteValue.Builder(objectLocation).build();
                client.execute(delete);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void updRoomUserlist(DcRoomEntity room, boolean delUser) {


        try {
            Set<String> userlist = RMemoryAPI.getInstance()
                    .pullSetElemFromMemory(Constants.USER_KEY + "room:users:" + room.id_room);


            if(delUser){
                userlist.removeAll(room.userlist);
                delRoomMessageByIDUsers(client, room);
            } else {
                userlist.addAll(room.userlist);
            }
            Namespace bucket = new Namespace("inbox", "dc_room");
            Location location = new Location(bucket, room.id_room);

            SetUpdate users = new SetUpdate();
            RMemoryAPI.getInstance().delFromMemory(Constants.USER_KEY + "room:users:" + room.id_room);
            userlist.forEach(usr -> {
                users.add(usr);
                RMemoryAPI.getInstance()
                        .pushSetElemToMemory(Constants.USER_KEY + "room:users:" + room.id_room, usr);
            });

            MapUpdate mu = new MapUpdate()
                    .update("userlist", users)
                    .update("last_upd", new RegisterUpdate(System.currentTimeMillis() + ""));
            UpdateMap update = new UpdateMap.Builder(location, mu)
                    .build();
            client.execute(update);

        } catch (Exception e) {
            log.error("Error while updating user updRoomUserlist()", e);
        }
    }

    @Override
    public RoomUsersInfo bldConversation(String idAuthor, String idUser) {

        String key = "";
        RoomUsersInfo usersInfo = new RoomUsersInfo();
        try {

            String idDstBox = RMemoryAPI.getInstance().pullHashFromMemory(Constants.USER_KEY + idUser, "inbox");
            String idSrcBox = RMemoryAPI.getInstance().pullHashFromMemory(Constants.USER_KEY + idAuthor, "inbox");

            key = RMemoryAPI.getInstance().pullElemFromMemory(Constants.USER_KEY + "room:keys:" + idAuthor+":"+idUser);


            Namespace bucket = new Namespace("inbox", "dc_room");
            if(key==null || key.isEmpty()) {
                MapUpdate mu = new MapUpdate()
                        .update("userlist", new SetUpdate().add(idAuthor).add(idUser))
                        .update("group", new FlagUpdate(false))
                        .update("id_author", new RegisterUpdate(idAuthor))
                        .update("last_msg", new RegisterUpdate(""))
                        .update("last_upd", new RegisterUpdate(System.currentTimeMillis() + ""));
                UpdateMap update = new UpdateMap.Builder(bucket, mu)
                        .build();
                key = client.execute(update).getGeneratedKey().toString();
                {
                    buildInbox(idDstBox, idUser, key, false);
                    buildInbox(idSrcBox, idAuthor, key, false);
                    RMemoryAPI.getInstance().pushSetElemToMemory(Constants.USER_KEY + "room:users:" + key, idAuthor);
                    RMemoryAPI.getInstance().pushSetElemToMemory(Constants.USER_KEY + "room:users:" + key, idUser);
                    RMemoryAPI.getInstance().pushElemToMemory(Constants.USER_KEY + "room:keys:" + idAuthor + ":" + idUser, -1, key);
                    RMemoryAPI.getInstance().pushElemToMemory(Constants.USER_KEY + "room:keys:" + idUser+":"+idAuthor, -1, key);


                    roomInfoHelper(usersInfo,idAuthor
                            ,new HashSet<>(Arrays.asList(idAuthor, idUser))
                            ,0L
                            ,""
                            ,""
                            ,key);
                }
            } else {
                Location location = new Location(bucket, key);
                FetchMap fetch = new FetchMap.Builder(location).build();
                FetchMap.Response response = client.execute(fetch);
                if (response != null) {
                    RiakMap map = response.getDatatype();

                    Set<BinaryValue> ulist = map.getSet("userlist").view();
                    Set<String> userlist = new HashSet<>();
                    userlist.addAll(ulist.stream().map(BinaryValue::toString).collect(Collectors.toList()));

                    idAuthor = map.getRegister("id_author").view().toString();
                    String cntVal = RMemoryAPI.getInstance()
                            .pullElemFromMemory(Constants.USER_KEY + "inbox:new:cnt:" + idAuthor + "_" + key);
                    Long msg_count = cntVal==null?0:Long.valueOf(cntVal);
                    String last_msg = map.getRegister("last_msg").view().toString();
                    String msg_type = "1";
                    if(map.getRegister("msg_type")!=null){
                        msg_type = map.getRegister("msg_type").view().toString();
                    }
                    roomInfoHelper(usersInfo,idAuthor,userlist,msg_count,last_msg,msg_type, key);
                }
            }

        } catch (Exception e) {
            log.error("Error while updating user bldConversation()", e);
        }

        return usersInfo;
    }

    protected void roomInfoHelper(RoomUsersInfo usersInfo,String idAuthor, Set<String> userlist,
                                  Long msgCount, String last_msg, String msg_type, String id_room){
        List<RoomShortInfo> shortInfoList = new ArrayList<>(3);
        userlist.forEach(idu -> {
            RoomShortInfo shortInfo = new RoomShortInfo();
            String ava = RMemoryAPI.getInstance().pullHashFromMemory(Constants.USER_KEY + idu, "avatar");
            shortInfo.avatar = Constants.STATIC_URL + idu + "/image" + ava + ".jpg";
            shortInfo.username = RMemoryAPI.getInstance().pullHashFromMemory(Constants.USER_KEY + idu, "username");
            shortInfo.id_inbx = RMemoryAPI.getInstance().pullHashFromMemory(Constants.USER_KEY + idu, "inbox");
            shortInfo.idUser = idu;
            shortInfoList.add(shortInfo);
        });

        usersInfo.data = shortInfoList;
        usersInfo.id_room = id_room;
        usersInfo.idAuthor = idAuthor;
        usersInfo.last_upd = System.currentTimeMillis() + "";
        usersInfo.last_msg = last_msg;
        usersInfo.msg_type = msg_type;
        usersInfo.msg_count = msgCount;
    }

    @Override
    public void bldComment(DcCommentsEntity comment) {

        try {
            Location location = new Location(new Namespace("dc_comments"), "dc_comment:" + comment.idComment);
            StoreValue sv = new StoreValue.Builder(comment)
                    .withLocation(location)
                    .build();
            client.execute(sv);

        } catch (Exception e) {
            log.error("Error while updating user bldComment()", e);
        }
    }

    @Override
    public void storeInboxMessage(DcMessageEntity entity) {

        try {

            Location key = new Location(new Namespace("dc_messages"), "dc_msg:" + entity.id_msg);
            //String url = RMemoryAPI.getInstance().pullElemFromMemory(Constants.INBX_MESSAGE+entity.id_msg);
            entity.dateadded = System.currentTimeMillis()+"";

            StoreValue sv = new StoreValue.Builder(entity)
                    .withLocation(key)
                    .build();
            client.execute(sv);

            Set<String> users = RMemoryAPI.getInstance()
                    .pullSetElemFromMemory(Constants.USER_KEY + "room:users:" + entity.id_room);
            users.forEach(idu -> {
                String idinbx = RMemoryAPI.getInstance().pullHashFromMemory(Constants.USER_KEY + idu, "inbox");
                RMemoryAPI.getInstance()
                        .pushSetElemToMemory(Constants.USER_KEY + "inbox:room:" + idinbx, entity.id_room);

                RMemoryAPI.getInstance()
                        .pushListElemToMemory(Constants.USER_KEY + "inbox:new:" + entity.idUser + "_" + entity.id_room, entity.id_msg, 99);

            });

            MessageAckUtils ackUtils = new MessageAckUtils();
            ackUtils.saveMessageCountStatus(entity.id_room, entity.idUser+"");

            DcRoomEntity roomEntity = new DcRoomEntity();
            roomEntity.setId_room(entity.id_room);
            roomEntity.setLast_msg(entity.body);
            roomEntity.setIdAuthor(entity.idUser+"");
            roomEntity.setMsg_type(entity.msg_type+"");
            log.info("Storing room info {}", roomEntity.toString());
            updConversation(roomEntity);

        } catch (Exception e) {
            log.error("Error while updating storeInboxMessage()", e);
        }
    }



    @Override
    public Integer buildMediaContent(DcMediaEntity entity){

        Integer updated = 0;
        try {

            Location key = new Location(new Namespace("dc_media"), "dc_media:" + entity.idMedia);
            entity.lastUpdate = System.currentTimeMillis();
            StoreValue sv = new StoreValue.Builder(entity)
                    .withLocation(key)
                    .build();
            StoreValue.Response svResponse = client.execute(sv);

        } catch (Exception e) {
            log.error("Error while updating media buildMediaContent()", e);
        }
        return updated;
    }

    @Override
    public void buildMediaComments(DcCommentsEntity entity){

        try {

            Location key = new Location(new Namespace("dc_commentaries"), "dc_comment:" + entity.idComment);
            StoreValue sv = new StoreValue.Builder(entity)
                    .withLocation(key)
                    .build();
            StoreValue.Response svResponse = client.execute(sv);

        } catch (Exception e) {
            log.error("Error while updating media buildMediaComments()", e);
        }

    }

    @Override
    public void removeMediaComments(Long idComment){
        try {
            Location geniusQuote = new Location(new Namespace("dc_commentaries"), "dc_comment:" + idComment);
            DeleteValue delete = new DeleteValue.Builder(geniusQuote).build();
            client.execute(delete);
        } catch (Exception e) {
            log.error("Error while deleting media removeMediaComments()", e);
        }
    }

    @Override
    public Integer buildChannelContent(DcChannelsEntity entity) {

        Integer updated = 0;
        try {

            Location key = new Location(new Namespace("dc_channels"), "dc_channels:" + entity.idChannel);

            StoreValue sv = new StoreValue.Builder(entity)
                    .withLocation(key)
                    .build();
            StoreValue.Response svResponse = client.execute(sv);

        } catch (Exception e) {
            log.error("Error while updating media buildMediaContent()", e);
        }
        return updated;
    }

    @Override
    public void updChannelContent(DcChannelsEntity e) {
        try {
            Location key = new Location(new Namespace("dc_channels"), "dc_channels:" + e.idChannel);
            UpdateValue updateOp = new UpdateValue.Builder(key)
                    .withFetchOption(FetchValue.Option.DELETED_VCLOCK, true)
                    .withUpdate(new DcChannelEntityUpdater(e))
                    .build();
            client.execute(updateOp);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void updMessageData(DcMessageEntity e) {
        try {
            Location key = new Location(new Namespace("dc_messages"), "dc_msg:" + e.id_msg);
            UpdateValue updateOp = new UpdateValue.Builder(key)
                    .withFetchOption(FetchValue.Option.DELETED_VCLOCK, true)
                    .withUpdate(new DcMessageEntityUpdater(e))
                    .build();
            client.execute(updateOp);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void updMediaContent(DcMediaEntity e) {
        try {
            RiakTP transport = RiakAPI.getInstance();
            IRiakQueryFactory queryFactory = new RiakQueryFactory(transport);
            DcMediaEntity entity = queryFactory.queryMediaDataByID(e.idMedia);

            log.info("RIAK video progress {}", entity.progress);
            if(entity.progress!=3) {
                Location key = new Location(new Namespace("dc_media"), "dc_media:" + e.idMedia);
                UpdateValue updateOp = new UpdateValue.Builder(key)
                        .withFetchOption(FetchValue.Option.DELETED_VCLOCK, true)
                        .withUpdate(new DcMediaEntityUpdater(e))
                        .build();
                UpdateValue.Response svResponse = client.execute(updateOp);

                log.info("Was Updated RIAK data {}, {}", e.idMedia, svResponse.wasUpdated());
            }
        } catch (Exception ex){
            ex.printStackTrace();
            log.error("Error in updated media content",ex);
        }
    }

    @Override
    public void removeMedia(Long idMedia) {
        try {
            Location key = new Location(new Namespace("dc_media"), "dc_media:" + idMedia);
            DeleteValue delete = new DeleteValue.Builder(key).build();
            client.execute(delete);
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Override
    public void storeMediaStatistics(Long idMedia, boolean init, String field){

        try {
            Namespace usersBucket = new Namespace("dc_media_stats", "activities");
            Location key = new Location(usersBucket, "dc_media:" + idMedia);
            MapUpdate mu;
            if(init) {
                mu = new MapUpdate()
                        .update("vnumber", new CounterUpdate(0))
                        .update("cnumber", new CounterUpdate(0))
                        .update("lkumber", new CounterUpdate(0))
                        .update("lvnumber", new CounterUpdate(0))
                        .update("rtnumber", new CounterUpdate(0)); //real-time viewers
            } else {
                mu = new MapUpdate()
                        .update(field, new CounterUpdate(1));
            }
            UpdateMap update = new UpdateMap.Builder(key, mu).build();
            client.execute(update);
        } catch (Exception e) {
            log.error("Error while updating media storeMediaStatistics()", e);
        }
    }

    @Override
    public void removeRoom(String idAthor, String idInbx, String id_room){
        try {
            buildInbox(idInbx, idAthor, id_room, true);
        } catch (Exception e) {
            log.error("Error while deleting user removeRoom()", e);
        }
    }
}
