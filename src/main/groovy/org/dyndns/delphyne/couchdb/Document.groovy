package org.dyndns.delphyne.couchdb

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass


/**
 * Maps the annotated class to a CouchDB database.  Each instance of this class represents a document in same-named
 *  database.  Will add a _rev property, and will either map a property annotated with {@link Key} to _id or will
 *  create a new property for this purpose.  Finally, adds create, read, and update methods on the class.
 * @author Brian M. Carr <delphyne@gmail.com>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass('org.dyndns.delphyne.couchdb.internal.KeyASTTransformation')
@interface Document {

}
