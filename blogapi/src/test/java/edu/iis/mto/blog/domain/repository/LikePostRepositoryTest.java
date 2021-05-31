package edu.iis.mto.blog.domain.repository;

import edu.iis.mto.blog.domain.model.AccountStatus;
import edu.iis.mto.blog.domain.model.BlogPost;
import edu.iis.mto.blog.domain.model.LikePost;
import edu.iis.mto.blog.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@DataJpaTest
public class LikePostRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private LikePostRepository likePostRepository;
    @Autowired
    private BlogPostRepository blogPostRepository;

    private User testUser;
    private BlogPost testBlogPost;

    @BeforeEach
    void setUp() {
        blogPostRepository.deleteAll();
        blogPostRepository.flush();
        likePostRepository.deleteAll();
        likePostRepository.flush();
        testUser = new User();
        testUser.setAccountStatus(AccountStatus.CONFIRMED);
        testUser.setEmail("kowalski@mail.com");
        testBlogPost = new BlogPost();
        testBlogPost.setEntry("Entry");
        testBlogPost.setUser(testUser);
        entityManager.persistAndFlush(testUser);
        entityManager.persistAndFlush(testBlogPost);
    }

    @Test
    void shouldSaveNewLikePost() {
        LikePost likePost = new LikePost();
        likePost.setUser(testUser);
        likePost.setPost(testBlogPost);
        LikePost persistedLikePost = likePostRepository.save(likePost);
        assertThat(persistedLikePost.getId(), notNullValue());
    }

    @Test
    void shouldAddLikePostToLikesInBlogPost() {
        LikePost likePost = new LikePost();
        likePost.setUser(testUser);
        likePost.setPost(testBlogPost);
        LikePost persistedLikePost = likePostRepository.save(likePost);

        entityManager.refresh(testBlogPost);
        assertEquals(testBlogPost.getLikesCount(), 1);
        assertEquals(testBlogPost.getLikes().get(0), persistedLikePost);
    }

    @Test
    void shouldFindLikePostSearchingByUserAndPost() {
        LikePost persistedLikePost = new LikePost();
        persistedLikePost.setUser(testUser);
        persistedLikePost.setPost(testBlogPost);
        entityManager.persistAndFlush(persistedLikePost);

        Optional<LikePost> optionalLikePost = likePostRepository.findByUserAndPost(testUser, testBlogPost);

        assertTrue(optionalLikePost.isPresent());
        LikePost likePost = optionalLikePost.get();
        assertEquals(likePost.getPost().getId(), testBlogPost.getId());
        assertEquals(likePost.getUser().getId(), testUser.getId());
    }

    @Test
    void shouldChangeBlogPostInLikePost() {
        LikePost persistedLikePost = new LikePost();
        persistedLikePost.setUser(testUser);
        persistedLikePost.setPost(testBlogPost);
        entityManager.persistAndFlush(persistedLikePost);

        BlogPost newBlogPost = new BlogPost();
        newBlogPost.setEntry("BlogPost Entry");
        newBlogPost.setUser(testUser);
        entityManager.persistAndFlush(newBlogPost);

        LikePost likePost = new LikePost();
        likePost.setId(persistedLikePost.getId());
        likePost.setPost(newBlogPost);
        likePost.setUser(testUser);
        likePostRepository.save(likePost);

        entityManager.flush();
        entityManager.refresh(persistedLikePost);
        assertEquals(persistedLikePost.getPost().getId(), newBlogPost.getId());
        assertEquals(persistedLikePost.getPost().getEntry(), "BlogPost Entry");
    }

    @Test
    void shouldChangeUserInLikePost() {
        LikePost persistedLikePost = new LikePost();
        persistedLikePost.setUser(testUser);
        persistedLikePost.setPost(testBlogPost);
        entityManager.persistAndFlush(persistedLikePost);

        User user = new User();
        user.setAccountStatus(AccountStatus.CONFIRMED);
        user.setFirstName("john");
        user.setLastName("brown");
        user.setEmail("brown@mail.com");
        entityManager.persistAndFlush(user);

        LikePost likePost = new LikePost();
        likePost.setId(persistedLikePost.getId());
        likePost.setPost(testBlogPost);
        likePost.setUser(user);
        likePostRepository.save(likePost);
        entityManager.flush();

        entityManager.refresh(persistedLikePost);
        assertEquals(persistedLikePost.getUser().getId(), user.getId());
        assertEquals(persistedLikePost.getUser().getFirstName(), "john");
        assertEquals(persistedLikePost.getUser().getLastName(), "brown");
        assertEquals(persistedLikePost.getUser().getEmail(), "brown@mail.com");
    }
}