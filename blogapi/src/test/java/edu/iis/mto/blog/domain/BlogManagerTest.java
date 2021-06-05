package edu.iis.mto.blog.domain;

import edu.iis.mto.blog.api.request.UserRequest;
import edu.iis.mto.blog.domain.errors.DomainError;
import edu.iis.mto.blog.domain.model.*;
import edu.iis.mto.blog.domain.repository.BlogPostRepository;
import edu.iis.mto.blog.domain.repository.LikePostRepository;
import edu.iis.mto.blog.domain.repository.UserRepository;
import edu.iis.mto.blog.services.BlogService;
import org.junit.jupiter.api.BeforeEach;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import edu.iis.mto.blog.domain.model.AccountStatus;
import edu.iis.mto.blog.domain.model.User;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class BlogManagerTest {

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private BlogPostRepository blogPostRepository;
    @MockBean
    private LikePostRepository likePostRepository;

    @Autowired
    private BlogService blogService;

    @Captor
    private ArgumentCaptor<LikePost> likePostParam;
    @Captor
    private ArgumentCaptor<User> userParam;

    private User user;
    private User user2;
    private BlogPost dummyBlogPost;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setAccountStatus(AccountStatus.CONFIRMED);
        user.setEmail("test1@mail.com");
        user.setId(1L);
        user2 = new User();
        user2.setAccountStatus(AccountStatus.NEW);
        user2.setEmail("test2@mail.com");
        user2.setId(2L);
        dummyBlogPost = new BlogPost();
        dummyBlogPost.setEntry("Entry");
        dummyBlogPost.setUser(user);
        dummyBlogPost.setId(1L);
    }

    @Test
    void creatingNewUserShouldSetAccountStatusToNEW() {
        blogService.createUser(new UserRequest("John", "Steward", "john@domain.com"));
        verify(userRepository).save(userParam.capture());
        User user = userParam.getValue();
        assertThat(user.getAccountStatus(), equalTo(AccountStatus.NEW));
    }

    @Test
    void shouldThrowDomainErrorWhenUnconfirmedUserLikesPost() {
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(blogPostRepository.findById(any())).thenReturn(Optional.of(dummyBlogPost));

        assertThrows(DomainError.class, () -> blogService.addLikeToPost(user.getId(), dummyBlogPost.getId()));
    }

    @Test
    void shouldAddLikeToPostWhenUserIsConfirmed() {
        user2.setAccountStatus(AccountStatus.CONFIRMED);

        when(userRepository.findById(any(Long.class))).thenReturn(Optional.of(user2));
        when(blogPostRepository.findById(any(Long.class))).thenReturn(Optional.of(dummyBlogPost));
        when(likePostRepository.findByUserAndPost(any(User.class), any(BlogPost.class))).thenReturn(Optional.empty());

        blogService.addLikeToPost(user2.getId(), dummyBlogPost.getId());

        verify(likePostRepository).save(likePostParam.capture());
        List<LikePost> capturedLikePosts = likePostParam.getAllValues();
        int expected = 1;
        assertEquals(expected, capturedLikePosts.size());
        assertThrows(DomainError.class, () -> blogService.addLikeToPost(user.getId(), dummyBlogPost.getId()));
        LikePost capturedLikePost = capturedLikePosts.get(0);
        assertEquals(user2, capturedLikePost.getUser());
        assertEquals(dummyBlogPost, capturedLikePost.getPost());
    }
}
