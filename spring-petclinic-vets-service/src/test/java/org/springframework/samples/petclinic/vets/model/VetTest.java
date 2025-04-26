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
    void testAddDuplicateSpecialtyObject() {
        Specialty s1 = new Specialty();
        s1.setName("radiology");

        vet.addSpecialty(s1);
        assertEquals(1, vet.getNrOfSpecialties(), "Count should be 1 after adding first specialty object");

        // Add the exact same object instance again
        vet.addSpecialty(s1);
        assertEquals(1, vet.getNrOfSpecialties(), "Adding the exact same specialty object again should not increase count");
    }

    @Test
    void testAddLogicallyEquivalentButDifferentSpecialtyObjects() {
         Specialty s1 = new Specialty();
         s1.setName("radiology");
         vet.addSpecialty(s1);
         assertEquals(1, vet.getNrOfSpecialties(), "Count should be 1 after adding first specialty object");

         // Create a new object with the same name
         Specialty s1LogicalDup = new Specialty();
         s1LogicalDup.setName("radiology");

         // Add the different object
         vet.addSpecialty(s1LogicalDup);

         // Because Specialty doesn't override equals/hashCode, the HashSet treats s1 and s1LogicalDup as distinct based on object identity.
         assertEquals(2, vet.getNrOfSpecialties(), "Adding a different specialty object with same name should increase count due to default equals/hashCode");
         assertTrue(vet.getSpecialties().contains(s1), "List should contain the first object");
         assertTrue(vet.getSpecialties().contains(s1LogicalDup), "List should contain the second, logically equivalent object");
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