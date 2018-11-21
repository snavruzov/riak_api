package com.brocast.riak.api.dao;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.core.RiakCluster;
import org.slf4j.LoggerFactory;


/**
 * BroCast.
 * Copyright: Sardor Navruzov
 * 2013-2016.
 */
public class RiakAPI {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RiakAPI.class);
    private static RiakTP riakTP = null;



    public RiakAPI() {}

    public static RiakTP getInstance() {
        if(riakTP==null){
            riakTP = new RiakTP();
            RiakCluster cluster = RiakClusterBuilder.getClusterInstance();
            riakTP.client = new RiakClient(cluster);
            riakTP.cluster = cluster;
        }
        return riakTP;
    }

    public static void close(RiakClient riak) {
        riak.shutdown();
        riak.cleanup();
    }
}

