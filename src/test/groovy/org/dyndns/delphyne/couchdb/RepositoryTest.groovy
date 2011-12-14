package org.dyndns.delphyne.couchdb

import java.security.SecureRandom

import org.dyndns.delphyne.couchdb.exception.ObjectDeletedException;
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
        Grouch person = repo.findDocument(Grouch, 'carrbm1')
        assert person._id == 'carrbm1'
        assert person.age == 33
        assert person.name == 'Brian M. Carr'
    }
    
    @Test
    void testFindByKeyWithSpaceInKey() {
        Grouch person = repo.findDocument(Grouch, 'cat be cool')
        assert person._id == 'cat be cool'
        assert person.age == 21
        assert person.name == 'My ID Has Spaces'
    }
    
    @Test
    void testCreateWithNoKey() {
        String personName = "New Person With Couch-Provided ID #${new BigInteger(128,random)}"
        Grouch beforeSave = new Grouch(name: personName, age: new BigInteger(6, random))
        Grouch afterSave = repo.saveDocument(Grouch, beforeSave)
        assert beforeSave.name == afterSave.name
        assert beforeSave.age == afterSave.age
        assert afterSave._id
        assert afterSave._rev
    }
    
    @Test
    void testCreateWithProvidedKey() {
        String id = new BigInteger(128, random)
        String personName = "New Person With Randomly Generated Key #${id}"
        Grouch beforeSave = new Grouch(name: personName, age: new BigInteger(6, random), _id: id)
        Grouch afterSave = repo.saveDocument(Grouch, beforeSave)
        assert beforeSave.name == afterSave.name
        assert beforeSave.age == afterSave.age
        assert afterSave._id == id
        assert afterSave._rev
    }

    @Test
    void testUpdate() {
        String id = new BigInteger(128, random)
        String personName = "New Person With Couch-Provided ID to Test Updates"
        Grouch newPerson = repo.saveDocument(Grouch, new Grouch(name: "New Person #${id} to Test Updates", age: 15))
        
        assert newPerson._id
        assert newPerson._rev
        
        newPerson.age = 25
        Grouch afterUpdate = repo.saveDocument(Grouch, newPerson)
        
        assert newPerson._id == afterUpdate._id
        assert newPerson._rev < afterUpdate._rev
        
        assert 25 == repo.findDocument(Grouch, newPerson._id).age
    }
    
    @Test
    void testDelete() {
        Grouch person = new Grouch(name: 'bob', age: 33)
        Grouch afterSave = repo.saveDocument(Grouch, person)
        
        assert afterSave._id
        assert afterSave._rev
        
        def afterDeleteRev = repo.deleteDocument(Grouch, afterSave)
        
        assert afterDeleteRev != null && afterDeleteRev != afterSave._rev
        
        try {
            repo.findDocument(Grouch, afterSave._id, false)
            assert false : 'This should have already thrown an ObjectDeletedException'
        } catch (ex) {
            assert ex instanceof ObjectDeletedException
        }
        
        try {
            repo.deleteDocument(Grouch, afterSave)
        } catch (ex) {
            assert ex instanceof ObjectDeletedException
        }
    }    
}

@ToString(includeNames=true)
class Grouch {
    String _id
    String _rev
    String name
    int age
}
