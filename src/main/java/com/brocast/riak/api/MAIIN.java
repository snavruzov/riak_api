package com.brocast.riak.api;

import java.util.HashSet;
import java.util.Set;

/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2016.
 */
public class MAIIN {

    public void main(String[] args){
        Set<String> room = new HashSet<>();
        room.add("232");
        room.add("343");
        room.add("222");

        String query = "idUser:(";
        for(String idu:room){
            query +=idu+" ";
        }
        query = query.concat(")");

        System.out.println(query);
    }
}
