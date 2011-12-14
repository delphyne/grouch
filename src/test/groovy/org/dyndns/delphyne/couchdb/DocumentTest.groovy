package org.dyndns.delphyne.couchdb

import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.tools.ast.TransformTestHelper
import org.dyndns.delphyne.couchdb.internal.DocumentASTTransformation
import org.junit.BeforeClass;
import org.junit.Test

class DocumentTest {
    static TransformTestHelper invoker = new TransformTestHelper(new DocumentASTTransformation(), CompilePhase.CANONICALIZATION)
    static Class grouch = invoker.parse '''
        @org.dyndns.delphyne.couchdb.Document
        class Grouch {
            @org.dyndns.delphyne.couchdb.Id String name
            int age
        }
    '''
    
    @BeforeClass
    static void setupConfig() {
        File configFile = new File('src/test/resources/repository-config.groovy')
        assert configFile.exists()
        
        ConfigSlurper slurper = new ConfigSlurper()
        ConfigObject co = slurper.parse(configFile.toURI().toURL())
        
        Repository.config = new RepositoryConfig(co)
    }
    
    @Test
    void testFind() {
        def instance = grouch.newInstance(name: 'joe', age: 33).find()
        assert 'joe' == instance._id
        assert 'joe' == instance.name
        assert 33 == instance.age
    }
}
