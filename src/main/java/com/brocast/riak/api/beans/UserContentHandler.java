package com.brocast.riak.api.beans;

import java.util.List;

/**
 * Created by sardor on 5/4/17.
 */
public class UserContentHandler {
    private List<DcMediaEntity> entities;
    private List<String> ids;

    public UserContentHandler(List<DcMediaEntity> entities, List<String> ids) {
        this.entities = entities;
        this.ids = ids;
    }

    public List<DcMediaEntity> getEntities() {
        return entities;
    }

    public void setEntities(List<DcMediaEntity> entities) {
        this.entities = entities;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if(ids!=null && !ids.isEmpty()){
            for(String id:ids){
                builder.append(id);
                builder.append(" ");
            }
        }

        return builder.toString();
    }
}
