package edu.iis.mto.blog.rest.test;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

public class GetPostTest extends FunctionalTests {

    private static final String USER_FIND_POST_API = "/blog/user/{id}/post";
    private static final Long BLOG_POST_CREATOR_ID = 1L;
    private static final Long REMOVED_USER_ID = 3L;

    @BeforeEach
    void createAndSaveBlogPost() {
        final String USER_POST_API = "/blog/user/{userId}/post";
        JSONObject jsonObj = new JSONObject().put("entry", "entry");
        given().accept(ContentType.JSON)
                .header("Content-Type", "application/json;charset=UTF-8")
                .body(jsonObj.toString())
                .post(USER_POST_API, BLOG_POST_CREATOR_ID);
    }

    @Test
    void shouldSuccessfullyGetUsersBlogPosts() {
        given().accept(ContentType.JSON)
                .header("Content-Type", "application/json;charset=UTF-8")
                .expect()
                .log()
                .all()
                .statusCode(HttpStatus.SC_OK)
                .when()
                .get(USER_FIND_POST_API, 1L);
    }

    @Test
    void shouldReturnStatusCode400WhenUserIsRemoved() {
        given().accept(ContentType.JSON)
                .header("Content-Type", "application/json;charset=UTF-8")
                .expect()
                .log()
                .all()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .when()
                .get(USER_FIND_POST_API, REMOVED_USER_ID);
    }
}