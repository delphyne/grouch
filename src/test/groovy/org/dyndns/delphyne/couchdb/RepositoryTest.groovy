package org.dyndns.delphyne.couchdb

import java.security.SecureRandom

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import groovy.transform.ToString

class RepositoryTest {
    Repository repo
    SecureRandom random = new SecureRandom()
    
    @BeforeClass
    static void setupConfig() {
        File configFile = new File('src/test/resources/repository-config.groovy')
        assert configFile.exists()
        
        ConfigSlurper slurper = new ConfigSlurper()
        ConfigObject co = slurper.parse(configFile.toURI().toURL())
        
        Repository.config = new RepositoryConfig(co)
    }
    
    @Before
    void setupRepo() {
        repo = new Repository()
    }
    
    @Test
    void testFindByKey() {
        Person person = repo.find(Person, 'carrbm1')
        assert person._id == 'carrbm1'
        assert person.age == 33
        assert person.name == 'Brian M. Carr'
    }
    
    @Test
    void testFindByKeyWithSpaceInKey() {
        Person person = repo.find(Person, 'cat be cool')
        assert person._id == 'cat be cool'
        assert person.age == 21
        assert person.name == 'My ID Has Spaces'
    }
    
    @Test
    void testCreateWithNoKey() {
        String personName = "New Person With Couch-Provided ID #${new BigInteger(128,random)}"
        Person beforeSave = new Person(name: personName, age: new BigInteger(6, random))
        Person afterSave = repo.save(Person, beforeSave)
        assert beforeSave.name == afterSave.name
        assert beforeSave.age == afterSave.age
        assert afterSave._id
        assert afterSave._rev
    }
    
    @Test
    void testCreateWithProvidedKey() {
        String id = new BigInteger(128, random)
        String personName = "New Person With Randomly Generated Key #${id}"
        Person beforeSave = new Person(name: personName, age: new BigInteger(6, random), _id: id)
        Person afterSave = repo.save(Person, beforeSave)
        assert beforeSave.name == afterSave.name
        assert beforeSave.age == afterSave.age
        assert afterSave._id == id
        assert afterSave._rev
    }

    @Test
    void testUpdate() {
        String id = new BigInteger(128, random)
        String personName = "New Person With Couch-Provided ID to Test Updates"
        Person newPerson = repo.save(Person, new Person(name: "New Person #${id} to Test Updates", age: 15))
        
        assert newPerson._id
        assert newPerson._rev
        
        newPerson.age = 25
        Person afterUpdate = repo.save(Person, newPerson)
        
        assert newPerson._id == afterUpdate._id
        assert newPerson._rev < afterUpdate._rev
        
        assert 25 == repo.find(Person, newPerson._id).age
    }
    
    @Test
    void testDelete() {
        Person person = new Person(name: 'bob', age: 33)
        Person afterSave = repo.save(Person, person)
        
        assert afterSave._id
        assert afterSave._rev
        
        def afterDeleteRev = repo.delete(Person, afterSave)
        
        assert afterDeleteRev != null && afterDeleteRev != afterSave._rev
        
        assert ! repo.find(Person, afterSave._id)
    }    
}

@ToString(includeNames=true)
class Person {
    String _id
    String _rev
    String name
    int age
}
