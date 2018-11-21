package com.brocast.riak.api.utils;

import com.dgtz.mcache.api.factory.Constants;
import com.dgtz.mcache.api.factory.RMemoryAPI;

import java.util.Set;

/**
 * Created by sardor on 4/28/17.
 */
public class MessageAckUtils {

    public MessageAckUtils() {
    }

    public void updateMessageReadStatus(String id_room, String idUser){
        Set<String> users = RMemoryAPI.getInstance().pullSetElemFromMemory(Constants.USER_KEY + "room:users:" + id_room);
        users.remove(idUser);
        users.forEach((String idu) ->
                RMemoryAPI.getInstance().delFromMemory(Constants.USER_KEY + "inbox:new:" + idu+"_"+id_room));
        resetMessageCountStatus(id_room, idUser);
    }


    public void saveMessageCountStatus(String id_room, String idUser){
        Set<String> users = RMemoryAPI.getInstance().pullSetElemFromMemory(Constants.USER_KEY + "room:users:" + id_room);
        users.remove(idUser);
        users.forEach((String idu) ->
                RMemoryAPI.getInstance()
                        .pushIncrToMemory(Constants.USER_KEY + "inbox:new:cnt:" + idu + "_" + id_room, 1));
    }

    public void resetMessageCountStatus(String id_room, String idUser){
        RMemoryAPI.getInstance()
                        .delFromMemory(Constants.USER_KEY + "inbox:new:cnt:" + idUser + "_" + id_room);
    }
}
