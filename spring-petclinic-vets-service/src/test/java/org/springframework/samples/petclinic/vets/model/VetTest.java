// src/test/java/org/springframework/samples/petclinic/vets/model/VetTest.java
package org.springframework.samples.petclinic.vets.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VetTest {

    private Vet vet;

    @BeforeEach
    void setUp() {
        vet = new Vet();
    }

    @Test
    void testSetAndGetId() {
        Integer idValue = 1;
        vet.setId(idValue);
        assertEquals(idValue, vet.getId(), "setId should correctly set the id");
    }

    @Test
    void testSetAndGetFirstName() {
        String testFirstName = "James";
        vet.setFirstName(testFirstName);
        assertEquals(testFirstName, vet.getFirstName(), "setFirstName should correctly set the first name");
    }

    @Test
    void testSetAndGetLastName() {
        String testLastName = "Carter";
        vet.setLastName(testLastName);
        assertEquals(testLastName, vet.getLastName(), "setLastName should correctly set the last name");
    }

    @Test
    void testGetSpecialtiesInternal_LazyInit() {
        // Access internal set via public methods that use it
        assertEquals(0, vet.getNrOfSpecialties(), "Initially, number of specialties should be 0");
        assertNotNull(vet.getSpecialties(), "getSpecialties should return an empty list, not null, even if internal set was null");
        assertTrue(vet.getSpecialties().isEmpty(), "Specialty list should be empty initially");
    }

    @Test
    void testAddSpecialty() {
        Specialty s1 = new Specialty();
        s1.setName("radiology");
        vet.addSpecialty(s1);
        assertEquals(1, vet.getNrOfSpecialties(), "Number of specialties should be 1 after adding one");
        assertTrue(vet.getSpecialties().contains(s1), "getSpecialties should contain the added specialty");
    }

    @Test
    void testAddMultipleSpecialties() {
        Specialty s1 = new Specialty();
        s1.setName("radiology");
        Specialty s2 = new Specialty();
        s2.setName("surgery");

        vet.addSpecialty(s1);
        vet.addSpecialty(s2);

        assertEquals(2, vet.getNrOfSpecialties(), "Number of specialties should be 2 after adding two");
        List<Specialty> specialties = vet.getSpecialties();
        assertTrue(specialties.contains(s1), "Specialties list should contain radiology");
        assertTrue(specialties.contains(s2), "Specialties list should contain surgery");
    }

    @Test
    void testAddDuplicateSpecialty() {
        Specialty s1 = new Specialty();
        s1.setName("radiology");
        s1.setId(1); // Assume ID makes it unique if equals/hashCode were based on it

        Specialty s1Dup = new Specialty();
        s1Dup.setName("radiology");
        s1Dup.setId(1);

        vet.addSpecialty(s1);
        vet.addSpecialty(s1Dup); // HashSet uses equals/hashCode, default is object identity

        // Without equals/hashCode, duplicates based on identity are added.
        // If s1 and s1Dup are different objects, they both get added.
        // Let's assume default equals/hashCode (identity)
        // assertEquals(1, vet.getNrOfSpecialties(), "Adding the 'same' specialty (by identity or default equals) should not increase count if it's the exact same object");
         vet.addSpecialty(s1); // Add the exact same object again
         assertEquals(1, vet.getNrOfSpecialties(), "Adding the exact same specialty object again should not increase count");


        // If we create two separate objects that are logically equivalent (same name/id)
        Specialty s2 = new Specialty();
        s2.setName("surgery");
        s2.setId(2);
        vet.addSpecialty(s2); // Now size is 2 (s1, s2)

        Specialty s2LogicalDup = new Specialty();
        s2LogicalDup.setName("surgery");
        s2LogicalDup.setId(2);
        vet.addSpecialty(s2LogicalDup); // Add a *different* object, but logically the same

        // Because Specialty doesn't override equals/hashCode, the HashSet treats s2 and s2LogicalDup as distinct
        assertEquals(3, vet.getNrOfSpecialties(), "Adding a different specialty object with same data should increase count due to default equals/hashCode");
        assertTrue(vet.getSpecialties().contains(s1));
        assertTrue(vet.getSpecialties().contains(s2));
        assertTrue(vet.getSpecialties().contains(s2LogicalDup)); // It contains the third object
    }

    @Test
    void testGetSpecialties_Sorting() {
        Specialty s1 = new Specialty();
        s1.setName("surgery"); // Should come after radiology
        Specialty s2 = new Specialty();
        s2.setName("radiology"); // Should come first
        Specialty s3 = new Specialty();
        s3.setName("dentistry"); // Should come before radiology

        vet.addSpecialty(s1);
        vet.addSpecialty(s2);
        vet.addSpecialty(s3);

        List<Specialty> specialties = vet.getSpecialties();
        assertEquals(3, specialties.size(), "Should have 3 specialties");
        assertEquals("dentistry", specialties.get(0).getName(), "First specialty should be dentistry after sorting");
        assertEquals("radiology", specialties.get(1).getName(), "Second specialty should be radiology after sorting");
        assertEquals("surgery", specialties.get(2).getName(), "Third specialty should be surgery after sorting");
    }

    @Test
    void testGetSpecialties_ReturnsUnmodifiableList() {
        Specialty s1 = new Specialty();
        s1.setName("surgery");
        vet.addSpecialty(s1);

        List<Specialty> specialties = vet.getSpecialties();

        assertThrows(UnsupportedOperationException.class, () -> {
            specialties.add(new Specialty()); // Try to modify the returned list
        }, "Returned specialty list should be unmodifiable");

         assertThrows(UnsupportedOperationException.class, () -> {
            specialties.remove(0); // Try to modify the returned list
        }, "Returned specialty list should be unmodifiable");
    }

    @Test
    void testGetNrOfSpecialties() {
        assertEquals(0, vet.getNrOfSpecialties(), "Initially should be 0");
        Specialty s1 = new Specialty();
        s1.setName("radiology");
        vet.addSpecialty(s1);
        assertEquals(1, vet.getNrOfSpecialties(), "Should be 1 after adding one specialty");
        Specialty s2 = new Specialty();
        s2.setName("surgery");
        vet.addSpecialty(s2);
        assertEquals(2, vet.getNrOfSpecialties(), "Should be 2 after adding a second specialty");
    }

}