package edu.iis.mto.blog.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.iis.mto.blog.api.request.UserRequest;
import edu.iis.mto.blog.dto.Id;
import edu.iis.mto.blog.services.BlogService;
import edu.iis.mto.blog.services.DataFinder;

import javax.persistence.EntityNotFoundException;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(BlogApi.class)
class BlogApiTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BlogService blogService;

    @MockBean
    private DataFinder finder;

    @Test
    public void postBlogUserShouldResponseWithStatusCreatedAndNewUserId() throws Exception {
        Long newUserId = 1L;
        UserRequest user = new UserRequest();
        user.setEmail("john@domain.com");
        user.setFirstName("John");
        user.setLastName("Steward");
        when(blogService.createUser(user)).thenReturn(newUserId);
        String content = writeJson(user);

        mvc.perform(post("/blog/user").contentType(MediaType.APPLICATION_JSON)
                                      .accept(MediaType.APPLICATION_JSON)
                                      .content(content))
           .andExpect(status().isCreated())
           .andExpect(content().string(writeJson(new Id(newUserId))));
    }

    private String writeJson(Object obj) throws JsonProcessingException {
        return new ObjectMapper().writer()
                                 .writeValueAsString(obj);
    }

    @Test
    public void shouldReturnStatusCode409WhenDataIntegrityViolationException() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail("test@domain.com");
        userRequest.setFirstName("John");
        userRequest.setLastName("Brown");

        when(blogService.createUser(userRequest)).thenThrow(DataIntegrityViolationException.class);
        String content = writeJson(userRequest);

        mvc.perform(post("/blog/user").contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(content))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldReturnStatusCode404WhenUserDoesntExist() throws Exception {
        Long testUserId = 1L;
        when(finder.getUserData(testUserId)).thenThrow(EntityNotFoundException.class);
        mvc.perform(get("/blog/user/" + testUserId)).andExpect(status().isNotFound());
    }
}
