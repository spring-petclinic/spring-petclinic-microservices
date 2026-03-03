package pets;

import base.BaseApiTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@DisplayName("Pets API")
class PetsIT extends BaseApiTest {

    private static final String OWNERS_ENDPOINT    = "/api/customer/owners";
    private static final String PET_TYPES_ENDPOINT = "/api/customer/petTypes";

    private int ownerId;

    @BeforeEach
    void createOwner() {
        ownerId = given().spec(writeSpec)
            .body(Map.of(
                "firstName", "Pet",
                "lastName",  "TestOwner",
                "address",   "123 Test Street",
                "city",      "New York",
                "telephone", "1234567890"
            ))
            .when()
            .post(OWNERS_ENDPOINT)
            .then()
            .statusCode(201)
            .extract().path("id");
    }

    private Map<String, Object> buildPetRequest(String name) {
        return Map.of(
            "name",      name,
            "birthDate", "2020-06-15",
            "typeId",    1
        );
    }

    private int createPet(String name) {
        return given().spec(writeSpec)
            .body(buildPetRequest(name))
            .when()
                .post(OWNERS_ENDPOINT + "/" + ownerId + "/pets")
            .then()
                .statusCode(201)
                .extract().path("id");
    }

    @Test
    @DisplayName("GET /petTypes returns 200 with all types")
    void getPetTypesReturns200() {
        given().spec(getSpec)
            .when()
                .get(PET_TYPES_ENDPOINT)
            .then()
                .statusCode(200)
                .body("$",    not(empty()))
                .body("id",   hasItems(1, 2, 3, 4, 5, 6))
                .body("name", hasItems("cat", "dog", "lizard", "snake", "bird", "hamster"));
    }

    @Test
    @DisplayName("GET /petTypes each type has id and name")
    void getPetTypesHasCorrectSchema() {
        given().spec(getSpec)
            .when()
                .get(PET_TYPES_ENDPOINT)
            .then()
                .statusCode(200)
                .body("every { it.containsKey('id') }",   is(true))
                .body("every { it.containsKey('name') }", is(true));
    }

    @Test
    @DisplayName("POST /owners/{ownerId}/pets creates pet and returns 201 with body")
    void createPetReturns201() {
        given().spec(writeSpec)
            .body(buildPetRequest("Whiskers"))
            .when()
                .post(OWNERS_ENDPOINT + "/" + ownerId + "/pets")
            .then()
                .statusCode(201)
                .body("id",        notNullValue())
                .body("name",      equalTo("Whiskers"))
                .body("birthDate", notNullValue())
                .body("type.id",   equalTo(1))
                .body("type.name", equalTo("cat"));
    }

    @Test
    @DisplayName("POST /owners/{ownerId}/pets response does not include owner field")
    void createPetResponseHasNoOwnerField() {
        // Pet entity has @JsonIgnore on owner — owner should not appear in POST response
        Response response = given().spec(writeSpec)
            .body(buildPetRequest("Whiskers"))
            .when()
                .post(OWNERS_ENDPOINT + "/" + ownerId + "/pets")
            .then()
                .statusCode(201)
                .extract().response();

        Object ownerValue = response.jsonPath().get("owner");
        assertThat(ownerValue).isNull();
    }

    @Test
    @DisplayName("POST /owners/{ownerId}/pets response time is under 2000ms")
    void createPetResponseTimeIsAcceptable() {
        given().spec(writeSpec)
            .body(buildPetRequest("Speedy"))
            .when()
                .post(OWNERS_ENDPOINT + "/" + ownerId + "/pets")
            .then()
                .statusCode(201)
                .time(lessThan(MAX_RESPONSE_TIME_MS));
    }

    @Test
    @DisplayName("GET /owners/*/pets/{petId} returns PetDetails with correct fields")
    void getPetByIdReturns200() {
        int petId = createPet("Luna");

        given().spec(getSpec)
            .when()
                .get(OWNERS_ENDPOINT + "/*/pets/" + petId)
            .then()
                .statusCode(200)
                .body("id",        equalTo(petId))
                .body("name",      equalTo("Luna"))
                .body("birthDate", notNullValue())
                .body("type.id",   equalTo(1))
                .body("type.name", equalTo("cat"));
    }

    @Test
    @DisplayName("GET /owners/*/pets/{petId} response includes owner full name")
    void getPetByIdIncludesOwnerName() {
        // PetDetails returns owner as a full name string (firstName + lastName)
        // not as an object — verify it is a non-null string
        int petId = createPet("Luna");

        Response response = given().spec(getSpec)
            .when()
                .get(OWNERS_ENDPOINT + "/*/pets/" + petId)
            .then()
                .statusCode(200)
                .extract().response();

        String owner = response.jsonPath().getString("owner");
        assertThat(owner).isNotBlank();
        assertThat(owner).contains("Pet");       // firstName from @BeforeEach
        assertThat(owner).contains("TestOwner"); // lastName from @BeforeEach
    }

    @Test
    @DisplayName("GET /owners/*/pets/{petId} response time is under 2000ms")
    void getPetByIdResponseTimeIsAcceptable() {
        int petId = createPet("Speedy");

        given().spec(getSpec)
            .when()
                .get(OWNERS_ENDPOINT + "/*/pets/" + petId)
            .then()
                .statusCode(200)
                .time(lessThan(MAX_RESPONSE_TIME_MS));
    }

    @Test
    @DisplayName("PUT /owners/*/pets/{petId} updates pet and returns 204")
    void updatePetReturns204() {
        int petId = createPet("OldName");

        Map<String, Object> updatedBody = Map.of(
            "id",        petId,
            "name",      "NewName",
            "birthDate", "2021-03-10",
            "typeId",    2           // change from cat to dog
        );

        given().spec(writeSpec)
            .body(updatedBody)
            .when()
                .put(OWNERS_ENDPOINT + "/*/pets/" + petId)
            .then()
                .statusCode(204);

        // Verify update persisted via subsequent GET
        given().spec(getSpec)
            .when()
                .get(OWNERS_ENDPOINT + "/*/pets/" + petId)
            .then()
                .statusCode(200)
                .body("name",      equalTo("NewName"))
                .body("type.id",   equalTo(2))
                .body("type.name", equalTo("dog"));
    }

    @Test
    @DisplayName("GET /owners/{ownerId} includes created pet in pets array")
    void ownerResponseIncludesPet() {
        // Verify the pet appears in the owner's pets array when fetching the owner
        int petId = createPet("Mittens");

        given().spec(getSpec)
            .when()
                .get(OWNERS_ENDPOINT + "/" + ownerId)
            .then()
                .statusCode(200)
                .body("pets",      not(empty()))
                .body("pets.id",   hasItem(petId))
                .body("pets.name", hasItem("Mittens"));
    }
}
