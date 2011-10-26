package org.dyndns.delphyne.couchdb

import org.junit.Test

class RepositoryTest {
    @Test
    void testFindPerson() {
        File configFile = new File('src/test/resources/repository-config.groovy')
        assert configFile.exists()
        
        ConfigSlurper slurper = new ConfigSlurper()
        ConfigObject co = slurper.parse(configFile.toURI().toURL())
        
        Repository repository = new Repository(config: new RepositoryConfig(co))
        Person person = repository.find(Person, 'carrbm1')
        assert person._id == 'carrbm1'
        assert person.age == 33
        assert person.name == 'Brian M. Carr'
    }
}

class Person {
    String _id
    String _rev
    String name
    int age
}
