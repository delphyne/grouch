package org.dyndns.delphyne.couchdb.exception

import groovy.transform.InheritConstructors;

@InheritConstructors
class ObjectNotFoundException extends Exception {
    ObjectNotFoundException(Throwable t) {
        super(t)
    }
}
