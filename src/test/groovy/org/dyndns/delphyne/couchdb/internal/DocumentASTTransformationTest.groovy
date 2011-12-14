package org.dyndns.delphyne.couchdb.internal

import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.tools.ast.TransformTestHelper
import org.dyndns.delphyne.couchdb.Repository
import org.junit.Test

import groovy.util.logging.Log

@Log
class DocumentASTTransformationTest {
    TransformTestHelper invoker = new TransformTestHelper(new DocumentASTTransformation(), CompilePhase.CANONICALIZATION) 
    
    /**
     * If a document has a field annotated with {@link Key}, then there should be no _id field and the get_id() and
     *  set_id() methods should proxy to whatever field is specified
     */
    @Test
    void testKeyAnnotation() {
        def clazz = invoker.parse('''
            @org.dyndns.delphyne.couchdb.Document
            class HasKey {
                @org.dyndns.delphyne.couchdb.Id String name
            }
        ''')

        assert ! clazz.declaredFields.find { Field it -> it.name == '_id' } : 'An _id field should not have been created if an @Id is specified'
        assert clazz.getDeclaredMethod('get_id', [] as Class[]) : 'A get_id() proxy method should have been created'
        assert clazz.getDeclaredMethod('set_id', [String] as Class[]) : 'A set_id() proxy method should have been created'

        def instance = clazz.newInstance([name: 'Brian'])
        assert instance.name == 'Brian'
        assert instance._id == instance.name : '_id should be an alias of the @Key annotated field'
        
        instance._id = 'Kate'
        assert instance.name == 'Kate' : 'setting the _id should change the @Key annotated field'
    }
    
    /**
     * If no @key is supplied, an _id property should be added
     */
    @Test
    void testNoKeyAnnotation() {
        def clazz = invoker.parse('''
            @org.dyndns.delphyne.couchdb.Document
            class HasNoKey {
            }
        ''')
        
        assert clazz.getDeclaredField('_id') : 'An _id field should be created if no @Key is provided'
        assert clazz.getDeclaredMethod('get_id', [] as Class[]) : '_id should be a full property'
        assert clazz.getDeclaredMethod('set_id', [String] as Class[]) : '_id should be a full property'
        
        def instance = clazz.newInstance([_id: '12345'])
        assert instance._id == '12345'
    }
    
    @Test
    void testRevProp() {
        def clazz = invoker.parse('''
            @org.dyndns.delphyne.couchdb.Document
            class HasRevProp {
            }
        ''')
        
        assert clazz.getDeclaredField('_rev') : 'A _rev property should always be provided'
        assert clazz.getDeclaredMethod('get_rev', [] as Class[]) : '_rev should be a full property'
        assert clazz.getDeclaredMethod('set_rev', [String] as Class[]) : '_rev should be a full property'
        
        def instance = clazz.newInstance([_rev: '1-abcdef'])
        assert instance._rev == '1-abcdef'
    }
    
    @Test
    void testMixinRepository() {
        def clazz = invoker.parse('''
            @org.dyndns.delphyne.couchdb.Document
            class HasRepositoryMethods {
            }
        ''')
        
        def instance = clazz.newInstance()
        
        //TODO: search for methods
    }
}
