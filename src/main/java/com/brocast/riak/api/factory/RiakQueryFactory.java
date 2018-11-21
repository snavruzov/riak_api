package com.brocast.riak.api.factory;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.datatypes.*;
import com.basho.riak.client.api.commands.kv.FetchValue;
import com.basho.riak.client.api.commands.search.Search;
import com.basho.riak.client.core.operations.SearchOperation;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.crdt.types.RiakMap;
import com.basho.riak.client.core.util.BinaryValue;
import com.brocast.riak.api.beans.*;
import com.brocast.riak.api.dao.RiakTP;
import com.dgtz.mcache.api.factory.Constants;
import com.dgtz.mcache.api.factory.RMemoryAPI;
import com.google.gson.GsonBuilder;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2016.
 */
public class RiakQueryFactory implements IRiakQueryFactory {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RiakQueryFactory.class);
    private RiakTP transport = null;
    private RiakClient client = null;

    public RiakQueryFactory(RiakTP transport) {
        this.transport = transport;
        this.client = this.transport.client;

    }

    @Override
    public List<DcRoomEntity> queryRoomList(String idinbx, String idUser) {

        List<DcRoomEntity> list = new ArrayList<>();
        try {

            Set<String> rlist = RMemoryAPI.getInstance()
                    .pullSetElemFromMemory(Constants.USER_KEY + "inbox:room:" + idinbx);

            Namespace bucket = new Namespace("inbox", "dc_room");

            rlist.forEach(idr -> {
                try {
                    Location location = new Location(bucket, idr);
                    FetchMap fetch = new FetchMap.Builder(location).build();
                    FetchMap.Response response = client.execute(fetch);
                    if (response != null) {
                        RiakMap map = response.getDatatype();

                        Set<BinaryValue> ulist = map.getSet("userlist").view();
                        Set<String> userlist = new HashSet<>();
                        userlist.addAll(ulist.stream().map(BinaryValue::toString).collect(Collectors.toList()));

                        String cntVal = RMemoryAPI.getInstance()
                                .pullElemFromMemory(Constants.USER_KEY + "inbox:new:cnt:" + idUser + "_" + idr);
                        Long msg_count = cntVal==null?0:Long.valueOf(cntVal);

                        DcRoomEntity room = new DcRoomEntity();
                        room.group = map.getFlag("group").view();
                        room.id_room = idr;
                        room.userlist = userlist;
                        room.msg_count = msg_count;
                        room.idAuthor = map.getRegister("id_author").view().toString();
                        room.last_msg = map.getRegister("last_msg").view().toString();
                        room.last_upd = map.getRegister("last_upd").view().toString();
                        room.id_inbx = idinbx;

                        userlist.forEach(idu -> {
                            if (!idu.equals(idUser)) {
                                String ava = RMemoryAPI.getInstance().pullHashFromMemory(Constants.USER_KEY + idu, "avatar");
                                room.avatar = Constants.STATIC_URL + idu + "/image" + ava + ".jpg";
                                room.username = RMemoryAPI.getInstance().pullHashFromMemory(Constants.USER_KEY + idu, "username");
                            }
                        });


                        list.add(room);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });


        } catch (Exception e) {

            log.error("Error while querying user queryRoomList()", e);
        }

        return list;
    }

    @Override
    public RoomUsersInfo queryRoomByIdRoom(String key) {

        RoomUsersInfo usersInfo = new RoomUsersInfo();
        try {
            Namespace bucket = new Namespace("inbox", "dc_room");
            Location location = new Location(bucket, key);
            FetchMap fetch = new FetchMap.Builder(location).build();
            FetchMap.Response response = client.execute(fetch);
            if (response != null) {
                RiakMap map = response.getDatatype();

                Set<BinaryValue> ulist = map.getSet("userlist").view();
                Set<String> userlist = new HashSet<>();
                userlist.addAll(ulist.stream().map(BinaryValue::toString).collect(Collectors.toList()));

                String idAuthor = map.getRegister("id_author").view().toString();
                String last_msg = map.getRegister("last_msg").view().toString();
                String msg_type = "1";
                if(map.getRegister("msg_type")!=null){
                    msg_type = map.getRegister("msg_type").view().toString();
                }

                log.info("Message type {}", msg_type);
                roomInfoHelper(usersInfo, idAuthor, userlist,last_msg,msg_type, key);
            }

        } catch (Exception e) {

            log.error("Error while querying user queryRoomList()", e);
        }

        return usersInfo;
    }

    protected void roomInfoHelper(RoomUsersInfo usersInfo,String idAuthor, Set<String> userlist
            , String last_msg, String msg_type, String id_room){
        List<RoomShortInfo> shortInfoList = new ArrayList<>(3);
        userlist.forEach(idu -> {
            RoomShortInfo shortInfo = new RoomShortInfo();
            String ava = RMemoryAPI.getInstance().pullHashFromMemory(Constants.USER_KEY + idu, "avatar");
            shortInfo.avatar = Constants.STATIC_URL + idu + "/image" + ava + ".jpg";
            shortInfo.username = RMemoryAPI.getInstance().pullHashFromMemory(Constants.USER_KEY + idu, "username");
            shortInfo.id_inbx = RMemoryAPI.getInstance().pullHashFromMemory(Constants.USER_KEY + idu, "inbox");
            shortInfo.idUser = idu;

            String cntVal = RMemoryAPI.getInstance()
                    .pullElemFromMemory(Constants.USER_KEY + "inbox:new:cnt:" + idu + "_" + id_room);
            Long msg_count = cntVal==null?0:Long.valueOf(cntVal);

            shortInfo.msg_count = msg_count;
            shortInfoList.add(shortInfo);
        });

        usersInfo.data = shortInfoList;
        usersInfo.id_room = id_room;
        usersInfo.idAuthor = idAuthor;
        usersInfo.last_upd = System.currentTimeMillis() + "";
        usersInfo.last_msg = last_msg;
        usersInfo.msg_count = 0L;
        usersInfo.msg_type = msg_type;
    }

    @Override
    public DcRoomEntity queryRoomByID(String key) {

        DcRoomEntity room = null;
        try {

            Location id_room = new Location(new Namespace("inbox", "dc_room"), key);

            FetchMap fetch = new FetchMap.Builder(id_room).build();
            FetchMap.Response response = client.execute(fetch);
            RiakMap map = response.getDatatype();

            Set<String> userlist = new HashSet<>();
            Set<BinaryValue> values = map.getSet("userlist").view();

            userlist.addAll(values.stream().map(BinaryValue::toString).collect(Collectors.toList()));

            room = new DcRoomEntity();
            room.id_inbx = map.getRegister("id_inbx").view().toString();
            room.id_room = key;
            room.userlist = userlist;
            room.idAuthor = map.getRegister("id_author").view().toString();

            String cntVal = RMemoryAPI.getInstance()
                    .pullElemFromMemory(Constants.USER_KEY + "inbox:new:cnt:" + room.idAuthor + "_" + key);
            Long msg_count = cntVal==null?0:Long.valueOf(cntVal);

            room.msg_count = msg_count;
            room.last_msg = map.getRegister("last_msg").view().toString();
            room.last_upd = map.getRegister("last_upd").view().toString();

        } catch (Exception e) {
            log.error("Error while querying user queryRoomByID()", e);
        }

        return room;
    }

    @Override
    public DcUsersEntity queryUserDataByIDUser(Long idUser) {

        DcUsersEntity obj = null;
        try {

            Namespace namespace = new Namespace("dc_users");
            Location objectLocation = new Location(namespace, "dc_users:" + idUser);
            FetchValue fetchOp = new FetchValue.Builder(objectLocation)
                    .build();

            FetchValue.Response res = client.execute(fetchOp);
            if(!res.isNotFound()){
                obj = res.getValue(DcUsersEntity.class);
            }

        } catch (Exception e) {

            log.error("Error while querying user queryUserDataByIDUser()", e);
        }

        return obj;
    }

    @Override
    /*Social account*/
    public DcUsersEntity queryUserDataByIDSocial(String idSocial, String type) {

        DcUsersEntity user = null;
        try {
            String field = "idFBSocial";
            switch (type) {
                case "GOOGLE": {
                    field = "idGSocial";
                    break;
                }
                case "TWITTER": {
                    field = "idTWTRSocial";
                    break;
                }
                case "VK": {
                    field = "idVKSocial";
                    break;
                }
            }
            Search searchOp = new Search
                    .Builder("users", field + ":" + idSocial)
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryUserDataByIDSocial: {}", results.toString());
                String bucket = results.get(0).get("_yz_rb").get(0);
                String key = results.get(0).get("_yz_rk").get(0);

                Location objectLocation = new Location(new Namespace(bucket), key);
                FetchValue fetchOp = new FetchValue.Builder(objectLocation)
                        .build();
                user = client.execute(fetchOp).getValue(DcUsersEntity.class);
            }

        } catch (Exception e) {
            log.error("Error while querying user queryUserDataByIDSocial()", e);
        }

        return user;
    }

    @Override
    public List<DcMediaEntity> queryMediaDataByTitle(String name, Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcMediaEntity> mlist = new ArrayList<>();
        try {
            name = name.replace(" ", "* ").concat("*");
            Search searchOp = new Search
                    .Builder("media", "title:(" + name + ")")
                    .filter("progress:0 AND idCategory:1")
                    .withStart(off)
                    .withRows(rows)
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryMediaDataByTitle: {}", results.size());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcMediaEntity media = client.execute(fetchOp).getValue(DcMediaEntity.class);
                        mlist.add(media);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryMediaDataByTitle()", e);
        }

        return mlist;
    }

    @Override
    public List<DcCommentsEntity> queryMediaComments(long duration, Long idMedia, int off, int rows, boolean reverse, boolean last) {
        rows = rows <= 100 ? rows : 100;
        List<DcCommentsEntity> mlist = new LinkedList<>();
        try {
            String query = "tkey:["+duration+" TO *]";
            String sort = "tkey asc";
            if(reverse){
                sort = "tkey desc";
                query = "tkey:[0 TO "+duration+"]";
            }
            if(last){
                sort = "idComment desc";
            }
            Search searchOp = new Search
                    .Builder("comments", query)
                    .filter("idMedia:"+idMedia)
                    .withStart(off)
                    .withRows(rows)
                    .sort(sort)
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryMediaComments: {}", results.size());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcCommentsEntity comment = client.execute(fetchOp).getValue(DcCommentsEntity.class);
                        mlist.add(comment);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryMediaComments()", e);
        }

        return mlist;
    }


    @Override
    public List<DcMediaEntity> queryMediaDataByCoordinate(String latlong, Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        String urlString = "http://"+Constants.RIAK_NODE_1+":8093/internal_solr/media";
        SolrClient solr = new HttpSolrClient.Builder(urlString).build();
        SolrQuery parameters = new SolrQuery();

        parameters.setQuery("progress:0 AND idCategory:1 AND idChannel:0");
        parameters.setFilterQueries("{!geofilt sfield=coordinate}");
        parameters.add("pt", latlong);
        parameters.add("d", "100");
        parameters.add("spatial", "true");
        parameters.setFields("idMedia");
        parameters.setStart(off);
        parameters.setRows(rows);
        parameters.addSort("lastUpdate", SolrQuery.ORDER.desc);

        QueryResponse response = null;
        List<DcMediaEntity> mlist = new ArrayList<>();
        try {
            response = solr.query(parameters);
            SolrDocumentList results = response.getResults();

            for (SolrDocument doc : results) {
                for (Map.Entry<String, Object> entry : doc) {

                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (key.equals("idMedia")) {
                        Location objectLocation = new Location(new Namespace("dc_media"), "dc_media:" + value);
                        FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                        try {
                            DcMediaEntity media = client.execute(fetchOp).getValue(DcMediaEntity.class);
                            if (media != null) {
                                mlist.add(media);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            solr.close();
        } catch (Exception e) {
            log.error("Error while querying user queryMediaDataByTitle()", e);
        }

        return mlist;
    }

    @Override
    public List<DcMediaEntity> queryMediaDataByTags(String tag, Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcMediaEntity> mlist = new ArrayList<>();
        try {

            tag = tag.replace(" ", "* ").concat("*");
            Search searchOp = new Search
                    .Builder("media", "tags:(" + tag + ")")
                    .filter("progress:0 AND idCategory:1")
                    .withStart(off)
                    .withRows(rows)
                    .sort("lastUpdate desc")
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryMediaDataByTags: {}", results.size());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcMediaEntity media = client.execute(fetchOp).getValue(DcMediaEntity.class);
                        mlist.add(media);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryMediaDataByTags()", e);
        }

        return mlist;
    }

    @Override
    public List<DcMediaEntity> queryMediaDataByPlace(String place, Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcMediaEntity> mlist = new ArrayList<>();
        try {

            place = place.replace(" ", "* ").concat("*");
            Search searchOp = new Search
                    .Builder("media", "location:(" + place + ")")
                    .filter("progress:0 AND idCategory:1")
                    .withStart(off)
                    .withRows(rows)
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryMediaDataByPlace: {}", results.size());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcMediaEntity media = client.execute(fetchOp).getValue(DcMediaEntity.class);
                        mlist.add(media);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryMediaDataByPlace()", e);
        }

        return mlist;
    }

    @Override
    public Set<String> queryTagDataByName(String name, Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        Set<String> mlist = new HashSet<>();
        try {
            String urlString = "http://"+Constants.RIAK_NODE_1+":8093/internal_solr/media";
            SolrClient solr = new HttpSolrClient.Builder(urlString).build();
            SolrQuery parameters = new SolrQuery();

           /*facet=on&facet.mincount=1&facet.field=tags&facet.limit=50*/
            parameters.setQuery("progress:0 AND idCategory:1 AND idChannel:0");

            parameters.addFacetField("tags");
            parameters.setFacetPrefix(name);
            parameters.setFacetLimit(rows);
            parameters.setFacetMinCount(1);
            parameters.setFacet(true);
            parameters.setRows(0);

            QueryResponse response = null;

            response = solr.query(parameters);
            List<FacetField> results = response.getFacetFields();

            for (FacetField doc : results) {
                mlist.addAll(doc.getValues()
                        .stream()
                        .map(FacetField.Count::getName)
                        .collect(Collectors.toList()));
            }

            log.info("Tags values: {}", mlist);

            solr.close();
        } catch (Exception e) {
            log.error("Error while querying user queryTagDataByName()", e);
        }

        return mlist;
    }

    @Override
    public List<DcMediaEntity> queryUserMediaDataByEvent(Long idUser, Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcMediaEntity> mlist = new ArrayList<>();
        try {

            Search searchOp = new Search
                    .Builder("media", "method:event")
                    .filter("idUser:"+idUser+" AND progress:[0 TO 1] AND idCategory:1")
                    .withStart(off)
                    .withRows(rows)
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryMediaDataByPlace: {}", results.size());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcMediaEntity media = client.execute(fetchOp).getValue(DcMediaEntity.class);
                        mlist.add(media);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryMediaDataByPlace()", e);
        }

        return mlist;
    }

    @Override
    public List<EventListEntity> queryUserEventList(Long idUser, Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<EventListEntity> mlist = new ArrayList<>();
        try {

            Search searchOp = new Search
                    .Builder("media", "method:event")
                    .filter("idUser:"+idUser+" AND progress:0 AND idCategory:1")
                    .withStart(off)
                    .withRows(rows)
                    .sort("dateadded desc")
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryUserEventList: {}", results.toString());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcMediaEntity media = client.execute(fetchOp).getValue(DcMediaEntity.class);
                        String time = RMemoryAPI.getInstance()
                                .pullHashFromMemory(Constants.MEDIA_KEY + media.idMedia, "evnt_time");
                        String chTitle = "";
                        if(media.idChannel!=0){
                            chTitle = RMemoryAPI.getInstance()
                                    .pullHashFromMemory(Constants.CHANNEL_KEY + media.idChannel, "title");
                        }

                        mlist.add(new EventListEntity(media.title
                                , media.idMedia+""
                                ,media.tags
                                ,time
                                ,chTitle
                                ,media.idChannel));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryMediaDataByPlace()", e);
        }

        return mlist;
    }

    @Override
    public Set<String> queryUsersByTOP(Integer rows) {
        rows = rows <= 100 ? rows : 100;
        Set<String> ulist = new HashSet<>();
        try {
            String urlString = "http://"+Constants.RIAK_NODE_1+":8093/internal_solr/media";
            SolrClient solr = new HttpSolrClient.Builder(urlString).build();

            SolrQuery parameters = new SolrQuery();
            parameters.setQuery("progress:0 AND idCategory:1 AND rating:[40 TO *] AND idUser:[1 TO *]");

            parameters.addFacetField("idUser");
            parameters.setFacetLimit(rows);
            parameters.setFacetMinCount(1);
            parameters.setFacet(true);
            parameters.setRows(0);

            QueryResponse response = null;
            try {
                response = solr.query(parameters);
                List<FacetField> results = response.getFacetFields();

                for (FacetField doc : results) {
                    ulist.addAll(doc.getValues()
                            .stream()
                            .map(FacetField.Count::getName)
                            .collect(Collectors.toList()));
                }
            } catch (Exception e) {
                log.error("Error while querying user queryMediaDataByTitle()", e);
            }

            solr.close();
        } catch (Exception e) {
            log.error("Error while querying user queryUsersByTOP()", e);
        }

        return ulist;
    }

    @Override
    public Long queryUsersChannelCountByIDUser(Long idUser) {
        Long num = 0l;
        try {
            Search searchOp = new Search
                    .Builder("channel", "enabled:true")
                    .filter("idUser:" + idUser)
                    .build();
            SearchOperation.Response res = client.execute(searchOp);
            num = (long)res.numResults();
        } catch (Exception e) {
            log.error("Error while querying user queryUsersChannelCountByIDUser()", e);
        }

        return num;
    }

    @Override
    public Long queryUsersVideoCountByIDUser(Long idUser) {
        Long num = 0l;
        try {
            Search searchOp = new Search
                    .Builder("media", "progress:[0 TO 1]")
                    .filter("idUser:" + idUser)
                    .build();
            SearchOperation.Response res = client.execute(searchOp);
            num = (long)res.numResults();
        } catch (Exception e) {
            log.error("Error while querying user queryUsersChannelCountByIDUser()", e);
        }

        return num;
    }

    @Override
    public Long queryUsersChannelMediaCountByIDChannel(Long idChannel) {
        Long num = 0l;
        try {
            Search searchOp = new Search
                    .Builder("media", "progress:[0 TO 1]")
                    .filter("idChannel:" + idChannel)
                    .build();
            SearchOperation.Response res = client.execute(searchOp);
            num = (long)res.numResults();
        } catch (Exception e) {
            log.error("Error while querying user queryUsersChannelCountByIDUser()", e);
        }

        return num;
    }

    @Override
    public Set<String> queryTagDataByTOP(Integer rows) {
        rows = rows <= 100 ? rows : 100;
        Set<String> mlist = new LinkedHashSet<>();
        try {
            String urlString = "http://"+Constants.RIAK_NODE_1+":8093/internal_solr/media";
            SolrClient solr = new HttpSolrClient.Builder(urlString).build();
            SolrQuery parameters = new SolrQuery();

        /*facet=on&facet.mincount=1&facet.field=tags&facet.limit=50*/
            parameters.setQuery("progress:0 AND idCategory:1 AND idChannel:0 AND rating:[50 TO *]");

            parameters.addFacetField("tags");
            parameters.setFacetLimit(rows);
            parameters.setFacetMinCount(1);
            parameters.setFacet(true);
            parameters.setRows(0);


            QueryResponse response = null;

            response = solr.query(parameters);
            List<FacetField> results = response.getFacetFields();

            for (FacetField doc : results) {
                mlist.addAll(doc.getValues()
                        .stream()
                        .map(FacetField.Count::getName)
                        .collect(Collectors.toList()));
            }

            solr.close();
        } catch (Exception e) {
            log.error("Error while querying user queryMediaDataByTitle()", e);
        }

        return mlist;
    }

    @Override
    public List<DcChannelsEntity> queryChannelDataByTitle(String name, Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcChannelsEntity> mlist = new ArrayList<>();
        try {

            name = name.replace(" ", "* ").concat("*");
            Search searchOp = new Search
                    .Builder("channel", "title:(" + name + ")")
                    .filter("enabled:true")
                    .withStart(off)
                    .withRows(rows)
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryChannelDataByTitle: {}", results.toString());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcChannelsEntity che = client.execute(fetchOp).getValue(DcChannelsEntity.class);
                        che.setAvatar(Constants.STATIC_URL + che.idUser + "/image" + che.getAvatar() + ".jpg");
                        if (che.getWall() == null || che.getWall().equals("empty")) {
                            che.setWall(Constants.STATIC_URL + "defaults/channel-cover.jpg");
                        } else {
                            che.setWall(Constants.STATIC_URL + che.idUser + "/image" + che.wall + ".jpg");
                        }
                        mlist.add(che);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryChannelDataByTitle()", e);
        }

        return mlist;
    }

    @Override
    public List<DcChannelsEntity> queryChannelDataByTitleByIDUser(String name, Long idUser, Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcChannelsEntity> mlist = new ArrayList<>();
        try {
            name = name.replace(" ", "* ").concat("*");
            Search searchOp = new Search
                    .Builder("channel", "title:(" + name + ")")
                    .filter("enabled:true AND idUser:"+idUser+" AND privacy:[0 TO 1]")
                    .withStart(off)
                    .withRows(rows)
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryChannelDataByTitle: {}", results.toString());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcChannelsEntity che = client.execute(fetchOp).getValue(DcChannelsEntity.class);
                        che.setAvatar(Constants.STATIC_URL + che.idUser + "/image" + che.getAvatar() + ".jpg");
                        if (che.getWall() == null || che.getWall().equals("empty")) {
                            che.setWall(Constants.STATIC_URL + "defaults/channel-cover.jpg");
                        } else {
                            che.setWall(Constants.STATIC_URL + che.idUser + "/image" + che.wall + ".jpg");
                        }
                        mlist.add(che);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryChannelDataByTitle()", e);
        }

        return mlist;
    }

    @Override
    public List<DcChannelsEntity> queryChannelDataByTitleByFollowing(String name, Long idUser, Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcChannelsEntity> mlist = new ArrayList<>();
        try {
            StringBuilder queryByChannel = new StringBuilder();
            Set<String> list = RMemoryAPI.getInstance().pullSetElemFromMemory(Constants.FOLLOWS + "channels:" + idUser);
            if (list != null && !list.isEmpty()) {
                queryByChannel.append("idChannel:(");
                for (String idu : list) {
                    queryByChannel.append(idu);
                    queryByChannel.append(" ");
                }
                queryByChannel.delete(queryByChannel.lastIndexOf(" "), queryByChannel.length());
                queryByChannel.append(") AND ");

                String qr = queryByChannel.toString();

                name = name.replace(" ", "* ").concat("*");
                Search searchOp = new Search
                        .Builder("channel", "enabled:true")
                        .filter(qr+"title:(" + name + ")")
                        .withStart(off)
                        .withRows(rows)
                        .build();
                SearchOperation.Response res = client.execute(searchOp);

                if (res.numResults() > 0) {
                    List<Map<String, List<String>>> results = res.getAllResults();
                    log.info("Getting search result of queryChannelDataByTitle: {}", results.toString());
                    results.forEach(doc -> {
                        String bucket = doc.get("_yz_rb").get(0);
                        String key = doc.get("_yz_rk").get(0);
                        Location objectLocation = new Location(new Namespace(bucket), key);
                        FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                        try {
                            DcChannelsEntity che = client.execute(fetchOp).getValue(DcChannelsEntity.class);
                            che.setAvatar(Constants.STATIC_URL + che.idUser + "/image" + che.getAvatar() + ".jpg");
                            if (che.getWall() == null || che.getWall().equals("empty")) {
                                che.setWall(Constants.STATIC_URL + "defaults/channel-cover.jpg");
                            } else {
                                che.setWall(Constants.STATIC_URL + che.idUser + "/image" + che.wall + ".jpg");
                            }
                            mlist.add(che);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });
                }
            }

        } catch (Exception e) {
            log.error("Error while querying user queryChannelDataByTitle()", e);
        }

        return mlist;
    }

    @Override
    public List<DcChannelsEntity> queryChannelDataByTOP(Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcChannelsEntity> mlist = new ArrayList<>();
        try {
            long timeQr = (System.currentTimeMillis() / 1000) - 15552000;
            Search searchOp = new Search
                    .Builder("channel", "enabled:true")
                    .filter("dateadded:[" + timeQr + " TO *]")
                    .sort("mcount desc, dateadded desc")
                    .withStart(off)
                    .withRows(rows)
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryChannelDataByTitle: {}", results.toString());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcChannelsEntity che = client.execute(fetchOp).getValue(DcChannelsEntity.class);
                        che.setAvatar(Constants.STATIC_URL + che.idUser + "/image" + che.getAvatar() + ".jpg");
                        if (che.getWall() == null || che.getWall().equals("empty")) {
                            che.setWall(Constants.STATIC_URL + "defaults/channel-cover.jpg");
                        } else {
                            che.setWall(Constants.STATIC_URL + che.idUser + "/image" + che.wall + ".jpg");
                        }
                        mlist.add(che);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryChannelDataByTitle()", e);
        }

        return mlist;
    }

    @Override
    public List<DcChannelsEntity> queryChannelDataByLast(Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcChannelsEntity> mlist = new ArrayList<>();
        try {
            Search searchOp = new Search
                    .Builder("channel", "*:*")
                    .filter("enabled:true")
                    .sort("dateadded desc")
                    .withStart(off)
                    .withRows(rows)
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryChannelDataByTitle: {}", results.toString());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcChannelsEntity che = client.execute(fetchOp).getValue(DcChannelsEntity.class);
                        che.setAvatar(Constants.STATIC_URL + che.idUser + "/image" + che.getAvatar() + ".jpg");
                        if (che.getWall() == null || che.getWall().equals("empty")) {
                            che.setWall(Constants.STATIC_URL + "defaults/channel-cover.jpg");
                        } else {
                            che.setWall(Constants.STATIC_URL + che.idUser + "/image" + che.wall + ".jpg");
                        }
                        mlist.add(che);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryChannelDataByTitle()", e);
        }

        return mlist;
    }

    @Override
    public List<DcChannelsEntity> queryChannelDataByIDuser(Long idUser, Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcChannelsEntity> mlist = new ArrayList<>();
        try {

            Search searchOp = new Search
                    .Builder("channel", "idUser:" + idUser)
                    .filter("enabled:true")
                    .withStart(off)
                    .withRows(rows)
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryChannelDataByTitle: {}", results.toString());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcChannelsEntity media = client.execute(fetchOp).getValue(DcChannelsEntity.class);
                        mlist.add(media);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryChannelDataByTitle()", e);
        }

        return mlist;
    }

    @Override
    public List<DcChannelsEntity> queryChannelDataByFollows(Long idUser, Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;

        List<DcChannelsEntity> mlist = new ArrayList<>();
        try {
            StringBuilder queryByChannel = new StringBuilder();
            Set<String> list = RMemoryAPI.getInstance().pullSetElemFromMemory(Constants.FOLLOWS + "channels:" + idUser);
            if (list != null && !list.isEmpty()) {
                queryByChannel.append("idChannel:(");
                for (String idu : list) {
                    queryByChannel.append(idu);
                    queryByChannel.append(" ");
                }
                queryByChannel.delete(queryByChannel.lastIndexOf(" "), queryByChannel.length());
                queryByChannel.append(")");

                String qr = String.valueOf(queryByChannel);
                Search searchOp = new Search
                        .Builder("channel", qr)
                        .filter("enabled:true")
                        .withStart(off)
                        .withRows(rows)
                        .build();
                SearchOperation.Response res = client.execute(searchOp);

                if (res.numResults() > 0) {
                    List<Map<String, List<String>>> results = res.getAllResults();
                    log.info("Getting search result of queryChannelDataByTitle: {}", results.toString());
                    results.forEach(doc -> {
                        String bucket = doc.get("_yz_rb").get(0);
                        String key = doc.get("_yz_rk").get(0);
                        Location objectLocation = new Location(new Namespace(bucket), key);
                        FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                        try {
                            DcChannelsEntity media = client.execute(fetchOp).getValue(DcChannelsEntity.class);
                            mlist.add(media);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });
                }
            }
        } catch (Exception e) {
            log.error("Error while querying user queryChannelDataByTitle()", e);
        }

        return mlist;
    }

    @Override
    public List<DcMediaEntity> queryRelatedVideos(Set<String> tags, Long idUser, Long idMedia) {
        List<DcMediaEntity> mlist = new ArrayList<>();
        try {
            UserContentHandler contentHandler = queryUserVideos(idUser, idMedia, 0, 12);
            mlist.addAll(contentHandler.getEntities());
            String query = "";
            if(mlist.size()<12) {
                String exclude = "";
                if(!mlist.isEmpty() && mlist.size()!=0){
                    exclude = " AND -idMedia:("+contentHandler.toString()+")";
                }
                System.out.println(exclude);
                if (tags != null && !tags.isEmpty()) {
                    StringBuilder queryByTags = new StringBuilder();
                    queryByTags.append("tags:(");
                    for (String tg : tags) {
                        queryByTags.append("\"").append(tg).append("\"");
                        queryByTags.append(" ");
                    }
                    queryByTags.delete(queryByTags.lastIndexOf(" "), queryByTags.length());
                    queryByTags.append(") OR ");
                    query = queryByTags.toString();
                }
                Search searchOp = new Search
                        .Builder("media", query + "rating:[50 TO *]")
                        .filter("progress:0 AND idCategory:1"+exclude)
                        .withStart(0)
                        .withRows(12 - mlist.size())
                        .sort("idMedia desc")
                        .build();
                SearchOperation.Response res = client.execute(searchOp);

                if (res.numResults() > 0) {
                    List<Map<String, List<String>>> results = res.getAllResults();
                    log.info("Getting search result of queryRelatedVideos: {}", results.size());
                    results.forEach(doc -> {
                        String bucket = doc.get("_yz_rb").get(0);
                        String key = doc.get("_yz_rk").get(0);
                        Location objectLocation = new Location(new Namespace(bucket), key);
                        FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                        try {
                            DcMediaEntity media = client.execute(fetchOp).getValue(DcMediaEntity.class);
                            mlist.add(media);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });
                }
            }

        } catch (Exception e) {
            log.error("Error while querying user queryRelatedVideos()", e);
        }

        return mlist;
    }

    @Override
    public List<DcMediaEntity> queryUserVideoDataByIduser(Long idUser, int off, int rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcMediaEntity> mlist = new ArrayList<>();
        try {
            Search searchOp = new Search
                    .Builder("media", "idUser:" + idUser)
                    .filter("progress:[0 TO 1] AND idCategory:1")
                    .withStart(off)
                    .withRows(rows)
                    .sort("dateadded desc")
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryUserVideos: {}", results.size());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcMediaEntity media = client.execute(fetchOp).getValue(DcMediaEntity.class);
                        mlist.add(media);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryUserVideoDataByIduser()", e);
        }

        return mlist;
    }

    private UserContentHandler queryUserVideos(Long idUser, Long idMedia, int off, int rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcMediaEntity> mlist = new ArrayList<>();
        List<String> idmlist = new ArrayList<>();
        UserContentHandler contentHandler = null;
        try {
            Search searchOp = new Search
                    .Builder("media", "idUser:" + idUser)
                    .filter("-idMedia:"+idMedia+" AND progress:0 AND idCategory:1")
                    .withStart(off)
                    .withRows(rows)
                    .sort("dateadded desc")
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryUserVideos: {}", results.size());
                System.out.println("Getting search result of queryUserVideos: {}"+ results.toString());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcMediaEntity media = client.execute(fetchOp).getValue(DcMediaEntity.class);
                        mlist.add(media);
                        idmlist.add(media.getIdMedia()+"");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryUserVideoDataByIduser()", e);
        }

        return new UserContentHandler(mlist, idmlist);
    }

    @Override
    public List<DcMediaEntity> queryMediaDataByIdChannel(Long idChannel, int off, int rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcMediaEntity> mlist = new ArrayList<>();
        try {
            Search searchOp = new Search
                    .Builder("media", "idChannel:" + idChannel)
                    .filter("progress:[0 TO 1]")
                    .withStart(off)
                    .withRows(rows)
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryMediaDataByIdChannel: {}", results.size());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcMediaEntity media = client.execute(fetchOp).getValue(DcMediaEntity.class);
                        mlist.add(media);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryMediaDataByIdChannel()", e);
        }

        return mlist;
    }

    @Override
    public List<DcMediaEntity> queryMediaByTOP(Integer offset, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcMediaEntity> mlist = new ArrayList<>();
        try {
            Search searchOp = new Search
                    .Builder("media", "rating:[50 TO *]")
                    .filter("progress:0 AND idCategory:1")
                    .withStart(offset)
                    .withRows(rows)
                    .sort("rating desc, dateadded desc")
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryMediaByTOP: {}", results.size());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcMediaEntity media = client.execute(fetchOp).getValue(DcMediaEntity.class);
                        mlist.add(media);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryMediaByTOP()", e);
        }

        return mlist;
    }

    @Override
    public List<DcMediaEntity> queryMediaByFeaturedTOP(Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcMediaEntity> mlist = new ArrayList<>();
        try {
            Search searchOp = new Search
                    .Builder("media", "rating:[1 TO 5]")
                    .filter("progress:0 AND idCategory:1")
                    .withStart(0)
                    .withRows(rows)
                    .sort("lastUpdate desc")
                    .build();
            SearchOperation.Response res = client.execute(searchOp);


            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryMediaByFeaturedTOP: {}", results.size());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcMediaEntity media = client.execute(fetchOp).getValue(DcMediaEntity.class);
                        mlist.add(media);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });

                RMemoryAPI.getInstance()
                        .pushElemToMemory(Constants.MEDIA_KEY + "toplist", 5, new GsonBuilder().create().toJson(mlist));
            }

        } catch (Exception e) {
            log.error("Error while querying user queryMediaByFeaturedTOP()", e);
        }

        return mlist;
    }

    @Override
    public List<DcMediaEntity> queryMediaByLast(Integer offset, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcMediaEntity> mlist = new ArrayList<>();
        try {
            Search searchOp = new Search
                    .Builder("media", "*:*")
                    .filter("progress:0 AND idCategory:1")
                    .withStart(offset)
                    .withRows(rows)
                    .sort("dateadded desc")
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryMediaByLast: {}", results.size());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcMediaEntity media = client.execute(fetchOp).getValue(DcMediaEntity.class);
                        mlist.add(media);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryMediaByLast()", e);
        }

        return mlist;
    }

    @Override
    public List<DcUsersEntity> queryUsersByLast(Integer offset, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcUsersEntity> ulist = new ArrayList<>();
        try {
            Search searchOp = new Search
                    .Builder("users", "*:*")
                    .withStart(offset)
                    .withRows(rows)
                    .sort("idUser desc")
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting result of queryUsersByLast: {}", results.toString());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcUsersEntity media = client.execute(fetchOp).getValue(DcUsersEntity.class);
                        ulist.add(media);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });
            }

        } catch (Exception e) {
            log.error("Error while querying user queryUsersByLast()", e);
        }

        return ulist;
    }

    @Override
    public List<DcMediaEntity> queryMediaByMAP(Integer offset, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcMediaEntity> mlist = new ArrayList<>();
        try {
            Search searchOp = new Search
                    //.Builder("media", "-coordinate:0,0")
                    .Builder("media", "*:*")
                    .filter("progress:0 AND idCategory:1")
                    .withStart(offset)
                    .withRows(rows)
                    .sort("dateadded desc")
                    .build();
            SearchOperation.Response res = client.execute(searchOp);


            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryMediaByMAP: {}", results.size());
                results.forEach(doc -> {
                    String bucket = doc.get("_yz_rb").get(0);
                    String key = doc.get("_yz_rk").get(0);
                    Location objectLocation = new Location(new Namespace(bucket), key);
                    FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                    try {
                        DcMediaEntity media = client.execute(fetchOp).getValue(DcMediaEntity.class);
                        mlist.add(media);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                });

                RMemoryAPI.getInstance()
                        .pushElemToMemory(Constants.MEDIA_KEY + "maplist", 6, new GsonBuilder().create().toJson(mlist));
            }

        } catch (Exception e) {
            log.error("Error while querying user queryMediaByMAP()", e);
        }

        return mlist;
    }

    @Override
    public DcCommentsEntity queryCommentByIDComment(Long idComment) {
        DcCommentsEntity comment = new DcCommentsEntity();
        try {
            Location location = new Location(new Namespace("dc_comments"), "dc_comment:" + idComment);
            FetchValue fv = new FetchValue.Builder(location).build();
            FetchValue.Response response = client.execute(fv);

            // Fetch object as Pojo class (map json to object)
            comment = response.getValue(DcCommentsEntity.class);

        } catch (Exception e) {
            log.error("Error while querying user queryCommentByIDComment()", e);
        }

        return comment;
    }

    @Override
    public Set<String> queryUserDataByName(String name, String method, Long idUser, Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        Set<String> ulist = new HashSet<>();
        Set<String> interset = null;
        switch (method) {
            case "follows": {
                interset = RMemoryAPI.getInstance().pullSetElemFromMemory(Constants.FOLLOWS + idUser);
                break;
            }
            case "followers": {
                interset = RMemoryAPI.getInstance().pullSetElemFromMemory(Constants.FOLLOWERS + idUser);
                break;
            }
            case "friends": {
                interset = RMemoryAPI.getInstance().pullSetElemFromMemory(Constants.FOLLOWS + idUser);
                Set<String> interset2 = RMemoryAPI.getInstance().pullSetElemFromMemory(Constants.FOLLOWERS + idUser);
                interset.retainAll(interset2);
                break;
            }
            case "all": {
                interset = Collections.emptySet();
                break;
            }
        }
        if (interset != null) {
            try {
                name = name.replace(" ", "\\ ").concat("*");
                name = "username:(*" + name.trim() + ")";

                log.info("query {}", name);
                Search searchOp = new Search
                        .Builder("users", "*:*")
                        .filter(name)
                        .withRows(100)
                        .build();
                SearchOperation.Response res = client.execute(searchOp);

                if (res.numResults() > 0) {
                    List<Map<String, List<String>>> results = res.getAllResults();
                    results.forEach(doc -> {
                        String bucket = doc.get("_yz_rb").get(0);
                        String key = doc.get("_yz_rk").get(0);
                        Location objectLocation = new Location(new Namespace(bucket), key);
                        FetchValue fetchOp = new FetchValue.Builder(objectLocation).build();
                        try {
                            DcUsersEntity user = client.execute(fetchOp).getValue(DcUsersEntity.class);
                            ulist.add(user.getIdUser() + "");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    });

                    log.info("Original Result riak by name {}", ulist);
                    if (!interset.isEmpty()) {
                        ulist.retainAll(interset);
                    }
                    log.info("After Result riak by name {}", ulist);

                }

            } catch (Exception e) {
                log.error("Error while querying user queryUserDataByIDSocial()", e);
            }
        }

        return ulist.stream()
                .skip(off)
                .limit(rows)
                .collect(Collectors.toSet());
    }


    protected void showDefaultCollection(Long idUser) {


        try {
            Search searchOp = new Search
                    .Builder("media", "rating:[40 TO *] OR rating:[1 TO 5] OR idUser:"+idUser+"")
                    .filter("idCategory:1 AND progress:0")
                    .withStart(0)
                    .withRows(500)
                    .sort("idMedia desc")
                    .build();

            SearchOperation.Response res = client.execute(searchOp);

            List<Map<String, List<String>>> results = res.getAllResults();
            //log.debug("Getting search result of showDefaultCollection: {}", results.toString());

            Set<String> ims = new HashSet<>();
            results.forEach(doc -> {
                String idMedia = doc.get("idMedia").get(0);
                ims.add(idMedia);
            });


            if(ims.size()>0) {
                RMemoryAPI.getInstance()
                        .pushSetElemToMemory(Constants.MEDIA_KEY + "collection:" + idUser, 300, ims.toArray(new String[ims.size()]));
            }

        } catch (Exception e) {
            log.error("Error while querying user showDefaultCollection()", e);
        }
    }

    @Override
    public void queryMediaCollection(Long idUser) {

        try {
            StringBuilder queryByUser = new StringBuilder();
            Set<String> list = RMemoryAPI.getInstance().pullSetElemFromMemory(Constants.FOLLOWS + idUser);
            if (list != null && !list.isEmpty()) {
                queryByUser.append("idUser:(");
                for (String idu : list) {
                    queryByUser.append(idu);
                    queryByUser.append(" ");
                }
                queryByUser.delete(queryByUser.lastIndexOf(" "), queryByUser.length());
                queryByUser.append(")√");
            }
            StringBuilder queryByTags = new StringBuilder();
            list = RMemoryAPI.getInstance().pullSetElemFromMemory(Constants.FOLLOWS + "tags:" + idUser);
            if (list != null && !list.isEmpty()) {
                queryByTags.append("tags:(");
                for (String tg : list) {
                    queryByTags.append("\"").append(tg).append("\"");
                    queryByTags.append(" ");
                }
                queryByTags.delete(queryByTags.lastIndexOf(" "), queryByTags.length());
                queryByTags.append(")√");
            }

            String qr = String.valueOf(queryByUser) + queryByTags;
            qr = qr.replace("√", " OR ");
            if (!qr.isEmpty() && qr.length() > 4) {
                log.debug("Built query ==> {}", qr + "rating:[50 TO *]");
                System.out.println(qr);
                Search searchOp = new Search
                        .Builder("media", qr + "rating:[50 TO *] OR idUser:" + idUser)
                        .filter("progress:0 AND idCategory:1")
                        .withStart(0)
                        .withRows(500)
                        .sort("lastUpdate desc, rating desc")
                        .build();
                SearchOperation.Response res = client.execute(searchOp);

                List<Map<String, List<String>>> results = res.getAllResults();

                Set<String> ims = new HashSet<>();
                results.forEach(doc -> {
                    String idMedia = doc.get("idMedia").get(0);
                    ims.add(idMedia);
                });


                RMemoryAPI.getInstance()
                        .pushSetElemToMemory(Constants.MEDIA_KEY + "collection:" + idUser, 30, ims.toArray(new String[ims.size()]));
            } else {
                showDefaultCollection(idUser);
            }

        } catch (Exception e) {
            log.error("Error while querying user media collection queryUserMediaCollection()", e);
        }

    }

    @Override
    public void queryUserMediaCollection(Long idUser) {

        try {
            StringBuilder queryByUser = new StringBuilder();
            Set<String> list = RMemoryAPI.getInstance().pullSetElemFromMemory(Constants.FOLLOWS + idUser);
            if (list != null && !list.isEmpty()) {
                queryByUser.append("idUser:(");
                list.forEach(idu -> {
                    queryByUser.append(idu);
                    queryByUser.append(" ");
                });
                queryByUser.delete(queryByUser.lastIndexOf(" "), queryByUser.length());
                queryByUser.append(")√");
            }
            /*StringBuilder queryByChannel = new StringBuilder();
            list = RMemoryAPI.getInstance().pullSetElemFromMemory(Constants.FOLLOWS + "channels:" + idUser);
            if (list != null && !list.isEmpty()) {
                queryByChannel.append("idChannel:(");
                for (String idu : list) {
                    queryByChannel.append(idu);
                    queryByChannel.append(" ");
                }
                queryByChannel.delete(queryByChannel.lastIndexOf(" "), queryByChannel.length());
                queryByChannel.append(")√");
            }*/
            StringBuilder queryByTags = new StringBuilder();
            list = RMemoryAPI.getInstance().pullSetElemFromMemory(Constants.FOLLOWS + "tags:" + idUser);
            if (list != null && !list.isEmpty()) {
                queryByTags.append("tags:(");
                list.forEach(tg -> {
                    queryByTags.append("\"").append(tg).append("\"");
                    queryByTags.append(" ");
                });
                queryByTags.delete(queryByTags.lastIndexOf(" "), queryByTags.length());
                queryByTags.append(")√");
            }

            String qr = String.valueOf(queryByUser) + queryByTags;
            qr = qr.replace("√", " OR ");
            if (!qr.isEmpty() && qr.length() > 4) {
                log.debug("Built query ==> {}", qr + "rating:[50 TO *]");
                System.out.println(qr);
                Search searchOp = new Search
                        .Builder("media", qr + "rating:[50 TO *] OR idUser:" + idUser)
                        .filter("progress:0 AND idCategory:1")
                        .withStart(0)
                        .withRows(500)
                        .sort("lastUpdate desc, rating desc")
                        .build();
                SearchOperation.Response res = client.execute(searchOp);

                List<Map<String, List<String>>> results = res.getAllResults();
                //log.debug("Getting search result of queryUserMediaCollection: {}", results.toString());

                Set<String> ims = new HashSet<>();
                results.forEach(doc -> {
                    String idMedia = doc.get("idMedia").get(0);
                    ims.add(idMedia);
                });


                RMemoryAPI.getInstance()
                        .pushSetElemToMemory(Constants.MEDIA_KEY + "collection:" + idUser, 30, ims.toArray(new String[ims.size()]));
            } else {
                showDefaultCollection(idUser);
            }

        } catch (Exception e) {
            log.error("Error while querying user media collection queryUserMediaCollection()", e);
        }

    }

    @Override
    /*Email account*/
    public DcUsersEntity queryUserDataByIDEmail(String email) {

        DcUsersEntity user = null;
        if (email == null || email.isEmpty()) {
            email = "none";
        }
        try {
            Search searchOp = new Search
                    .Builder("users", "email:" + email)
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            if (res.numResults() > 0) {
                List<Map<String, List<String>>> results = res.getAllResults();
                log.info("Getting search result of queryUserDataByIDEmail: {}", results.toString());
                String bucket = results.get(0).get("_yz_rb").get(0);
                String key = results.get(0).get("_yz_rk").get(0);

                Location objectLocation = new Location(new Namespace(bucket), key);
                FetchValue fetchOp = new FetchValue.Builder(objectLocation)
                        .build();
                user = client.execute(fetchOp).getValue(DcUsersEntity.class);
            }

        } catch (Exception e) {
            log.error("Error while querying user queryUserDataByIDEmail()", e);
        }

        return user;
    }

    /*
        int rowsPerPage = 2;
        int page = 2;
        int start = rowsPerPage * (page - 1);
    */
    @Override
    public List<DcMessageEntity> queryUserChatByIDRoom(String id_room, Integer off, Integer rows) {
        rows = rows <= 100 ? rows : 100;
        List<DcMessageEntity> msgs = new ArrayList<>();
        try {
            Search searchOp = new Search
                    .Builder("inbox", "id_room:" + id_room)
                    .withStart(off)
                    .withRows(rows)
                    .sort("dateadded desc")
                    .build();
            SearchOperation.Response res = client.execute(searchOp);

            List<Map<String, List<String>>> results = res.getAllResults();
            //log.info("Getting search result of inbox msgs: {}", results.toString());

            results.forEach(doc -> {
                String bucket = doc.get("_yz_rb").get(0);
                String key = doc.get("_yz_rk").get(0);

                Location objectLocation = new Location(new Namespace(bucket), key);
                FetchValue fetchOp = new FetchValue.Builder(objectLocation)
                        .build();
                try {
                    DcMessageEntity entity = client.execute(fetchOp).getValue(DcMessageEntity.class);
                    if (entity != null) {
                        entity.username = RMemoryAPI.getInstance()
                                .pullHashFromMemory(Constants.USER_KEY + entity.idUser, "username");

                        String ava = RMemoryAPI.getInstance()
                                .pullHashFromMemory(Constants.USER_KEY + entity.idUser, "avatar");
                        boolean unread = RMemoryAPI.getInstance()
                                .pullIfListElem(Constants.USER_KEY + "inbox:new:" + entity.idUser+"_"+id_room, entity.id_msg);
                        entity.avatar = Constants.STATIC_URL + entity.idUser + "/image" + ava + ".jpg";
                        entity.is_read = !unread;

                        switch (entity.msg_type.intValue()) {
                            case 4:{
                                entity.url = Constants.STATIC_URL + entity.url;
                                entity.thumb = Constants.STATIC_URL + entity.thumb;
                                break;
                            }
                            case 5: {
                                entity.url = Constants.STATIC_URL + entity.url;
                                break;
                            }
                            case 3: {
                                entity.url = Constants.VIDEO_URL + entity.url;
                                entity.thumb = Constants.STATIC_URL + entity.thumb;
                                break;
                            }
                            case 8: {
                                String idMSG = RMemoryAPI.getInstance()
                                        .pullElemFromMemory(Constants.LIVE_KEY + "debate.status:" + entity.idMedia);

                                log.info("MediaStatus {} idm {}", idMSG, entity.idMedia);
                                if (idMSG==null || !idMSG.equals(entity.id_msg)) {
                                    entity.status = "invalid";
                                }
                                break;
                            }
                            case 2: {
                                DcMediaEntity media = RMemoryAPI.getInstance()
                                        .pullHashFromMemory(Constants.MEDIA_KEY + entity.idMedia, "detail", DcMediaEntity.class);
                                if (media != null) {
                                    //entity.body = media.title; todo uncomment in prod
                                    entity.author_name = RMemoryAPI.getInstance().pullHashFromMemory(Constants.USER_KEY + media.idUser, "username");
                                    entity.idAuthor = media.idUser;
                                    entity.method = media.method;
                                    entity.thumb = Constants.encryptAmazonURL(media.getIdUser(), media.getIdMedia(), "jpg", "thumb", Constants.STATIC_URL);
                                } else {
                                    entity.status = "invalid";
                                }
                                break;
                            }
                        }

                        msgs.add(entity);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        } catch (Exception e) {
            log.error("Error while querying user queryUserChatByIDRoom()", e);
        }

        return msgs;
    }

    @Override
    public DcUserNotificationSettings queryUserNotificationSettings(Long idUser, String type) {

        DcUserNotificationSettings settings = null;
        try {

            if (!type.equals("push") && !type.equals("email")) {
                throw new Exception();
            }

            Location key = new Location(new Namespace("user_nsettings", type), "dc_users:" + idUser);

            MapUpdate mu = new MapUpdate()
                    .update("inbox", new FlagUpdate(true));
            UpdateMap update = new UpdateMap.Builder(key, mu).build();
            client.execute(update);

            FetchMap fetch = new FetchMap.Builder(key).build();
            FetchMap.Response response = client.execute(fetch);
            RiakMap map = response.getDatatype();

            if (map != null) {
                settings = new DcUserNotificationSettings();
                settings.channel = map.getFlag("channel").view();
                settings.live = map.getFlag("live").view();
                settings.promo = map.getFlag("promo").view();
                settings.follower = map.getFlag("follower").view();
                settings.comment = map.getFlag("comment").view();
                settings.inbox = map.getFlag("inbox").view();
            }
        } catch (Exception e) {

            log.error("Error while querying user queryUserNotificationSettings()", e);
        }

        return settings;
    }

    @Override
    public MediaStatistics queryMediaViewStatistics(Long idMedia) {

        MediaStatistics settings = null;
        try {

            Location key = new Location(new Namespace("dc_media_stats", "activities"), "dc_media:" + idMedia);

            FetchMap fetch = new FetchMap.Builder(key).build();
            FetchMap.Response response = client.execute(fetch);
            RiakMap map = response.getDatatype();

            if (map != null) {
                settings = new MediaStatistics();
                settings.record_viewers = map.getCounter("vnumber").view();
                settings.comments = map.getCounter("cnumber").view();
                settings.likes = map.getCounter("lkumber").view();
                settings.live_viewers = map.getCounter("lvnumber").view();
                settings.now_viewers = map.getCounter("rtnumber").view();
            }
        } catch (Exception e) {

            log.error("Error while querying user queryMediaViewStatistics()", e);
        }

        return settings;
    }

    @Override
    public DcMediaEntity queryMediaDataByID(Long idMedia) {

        DcMediaEntity obj = null;
        try {

            Namespace namespace = new Namespace("dc_media");
            Location objectLocation = new Location(namespace, "dc_media:" + idMedia);
            FetchValue fetchOp = new FetchValue.Builder(objectLocation)
                    .build();
            obj = client.execute(fetchOp).getValue(DcMediaEntity.class);

        } catch (Exception e) {

            log.error("Error while querying user queryMediaDataByID()", e);
        }

        return obj;
    }

    @Override
    public DcChannelsEntity queryChannelDataByID(Long idChannel) {

        DcChannelsEntity obj = null;
        try {

            Namespace namespace = new Namespace("dc_channels");
            Location objectLocation = new Location(namespace, "dc_channels:" + idChannel);
            FetchValue fetchOp = new FetchValue.Builder(objectLocation)
                    .build();
            obj = client.execute(fetchOp).getValue(DcChannelsEntity.class);

        } catch (Exception e) {

            log.error("Error while querying user queryMediaDataByID()", e);
        }

        return obj;
    }

}
