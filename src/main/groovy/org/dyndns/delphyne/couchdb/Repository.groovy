package org.dyndns.delphyne.couchdb

import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import groovy.util.logging.Log4j
import groovyx.net.http.RESTClient

/**
 * This class is Mixed into all classes annotated by Document in order to provide the create, update, retrieve, and 
 *  delete methods.
 * @author bcarr
 *
 */
@Log4j
class Repository {
    static RepositoryConfig config

    RESTClient couch

    Repository() {
        couch = new RESTClient(config.url)
        couch.parser
        couch.auth.basic(config.apikey.user, config.apikey.password)
    }

    /**
     * Find a Document on the table represented by the provided ID.  This method is provided by the Repository class,
     *  and is mixed into all {@link Document} annotated classes.
     * @param id
     * @return
     */
    def find(String id) {
        find(this.class, id)
    }

    private <T> T find(Class<T> type, String id) {
        def response = couch.get(path: "${type.simpleName.toLowerCase()}/${id}", contentType: 'text/plain')
        if (response.status == 200) {
            JsonSlurper slurper = new JsonSlurper()
            type.newInstance(slurper.parse(response.data))
        }
    }

    def save(def instance) {
        save(this.class, instance)
    }

    private <T> T save(Class<T> type, T instance) {
        def propMap = toMap(instance)
        def json = JsonOutput.toJson(propMap)

        boolean created
        def response
        if (instance._id == null) {
            response = couch.post(
                                            path: "${type.simpleName.toLowerCase()}/",
                                            contentType: 'text/plain',
                                            requestContentType: 'application/json',
                                            body: json)
            created = true
        } else {
            response = couch.put(
                                            path: "${type.simpleName.toLowerCase()}/${instance._id}",
                                            contentType: 'text/plain',
                                            requestContentType: 'application/json',
                                            body: json)
        }

        JsonSlurper slurper = new JsonSlurper()
        def responseMap = slurper.parse(response.data)
        propMap._id = responseMap.id
        propMap._rev = responseMap.rev
        type.newInstance(propMap)
    }

    private toMap(def instance) {
        instance.properties.inject([:]) { acc, it ->
            if (it.value == null) {
                log.trace "excluding ${it.key} due to null value"
                acc
            } else if (it.key ==~ /class|metaClass|metaClass/) {
                log.trace "suppressing ${it.key}"
                acc
            } else {
                acc << it
            }
        }
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