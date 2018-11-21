package com.brocast.riak.api.dao;

import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;
import com.dgtz.mcache.api.factory.Constants;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sardor on 6/22/17.
 */
public class RiakClusterBuilder {
    private static RiakCluster riakCluster = null;

    private RiakCluster setUpCluster() {
        List<RiakNode> nodes = new LinkedList<>();
        RiakNode.Builder nodeTemplate = new RiakNode.Builder()
                .withMinConnections(10)
                .withMaxConnections(50);
        nodes.add(nodeTemplate.withRemoteAddress(Constants.RIAK_NODE_1).withRemotePort(8087).build());
        //nodes.add(nodeTemplate.withRemoteAddress(Constants.RIAK_NODE_2).withRemotePort(8087).build());
        //nodes.add(nodeTemplate.withRemoteAddress("172.31.47.143").withRemotePort(8087).build());
        //nodes.add(nodeTemplate.withRemoteAddress("172.31.38.106").withRemotePort(8087).build());
        //nodes.add(nodeTemplate.withRemoteAddress("34.249.240.195").withRemotePort(8087).build());
        //nodes.add(nodeTemplate.withRemoteAddress("34.248.183.138").withRemotePort(8087).build());

        // Increase the # of execution attempts (retries) to 5, from the default of 3
        //log.debug("Riak nodes: {}", nodes);
        RiakCluster myCluster = RiakCluster.builder(nodes)
                .withExecutionAttempts(5)
                .build();

        myCluster.start();
        return myCluster;
    }

    public static RiakCluster getClusterInstance(){
        if(riakCluster==null){
            riakCluster = new RiakClusterBuilder().setUpCluster();
        }

        return riakCluster;
    }
}
