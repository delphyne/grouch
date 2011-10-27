package org.dyndns.delphyne.couchdb

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import org.codehaus.groovy.transform.GroovyASTTransformationClass

/**
 * Denotes the ID field of a couch db database
 * @author bcarr
 *
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
@interface Id { }
