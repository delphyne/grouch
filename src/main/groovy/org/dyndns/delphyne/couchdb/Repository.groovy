package org.dyndns.delphyne.couchdb

import groovy.json.JsonSlurper;
import groovyx.net.http.HTTPBuilder;

/**
 * This class is Mixed into all classes annotated by Document in order to provide the create, update, retrieve, and 
 *  delete methods.
 * @author bcarr
 *
 */
class Repository {
    RepositoryConfig config
    
    def find(String key) {
        find(this.class, key)
    }
    
    private <T> T find(Class<T> type, String key) {
        HTTPBuilder couch = new HTTPBuilder(config.url)
        couch.auth.basic(config.apikey.user, config.apikey.password)
        def response = couch.get(path: "${type.simpleName.toLowerCase()}/${key}")
        JsonSlurper slurper = new JsonSlurper()
        type.newInstance(slurper.parse(response))
    }
}

class RepositoryConfig {
    String url
    APIKey apikey
    
    class APIKey {
        String user
        String password
    }
}