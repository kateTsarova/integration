package edu.iis.mto.blog.rest.test;

import io.restassured.http.ContentType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import static org.hamcrest.Matchers.equalTo;

import static io.restassured.RestAssured.given;

public class LikePostTest extends FunctionalTests {

    private static final String CREATE_LIKE_POST_API = "/blog/user/{userId}/like/{postId}";
    @Test
    void shouldLikePostWhenUserIsConfirmed() {
        given().accept(ContentType.JSON)
                .header("Content-Type", "application/json;charset=UTF-8")
                .pathParams("userId", 1, "postId", 1)
                .expect()
                .log()
                .all()
                .statusCode(HttpStatus.SC_OK)
                .when()
                .post(CREATE_LIKE_POST_API);
    }
    @Test
    void shouldNotLikePostWhenUserIsItsAuthor() {
        given().accept(ContentType.JSON)
                .header("Content-Type", "application/json;charset=UTF-8")
                .pathParams("userId", 3, "postId", 1)
                .expect()
                .log()
                .all()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .when()
                .post(CREATE_LIKE_POST_API);
    }
    @Test
    void shouldNotLikePostWhenUserIsNotConfirmed() {
        given().accept(ContentType.JSON)
                .header("Content-Type", "application/json;charset=UTF-8")
                .pathParams("userId", 2, "postId", 1)
                .expect()
                .log()
                .all()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .when()
                .post(CREATE_LIKE_POST_API);
    }

    @Test
    void createLikePostByUserWhoAlreadyLikedPostReturnsFalse() {
        given().accept(ContentType.JSON)
                .header("Content-Type", "application/json;charset=UTF-8")
                .pathParams("userId", 4, "postId", 1)
                .expect()
                .log()
                .all()
                .statusCode(HttpStatus.SC_OK)
                .body(equalTo("false"))
                .when()
                .post(CREATE_LIKE_POST_API);
    }
}