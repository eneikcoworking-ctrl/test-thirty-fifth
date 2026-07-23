package com.bookmarks;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class BookmarkApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        bookmarkRepository.deleteAll();
        tagRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void testUserRegistrationAndLoginFlow() throws Exception {
        // 1. Register a new user
        UserRegistrationRequest regReq = new UserRegistrationRequest("jane_doe", "my_secure_password_123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message", is("User registered successfully")));

        // 2. Register same user again should fail (400 Bad Request)
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Bad Request")));

        // 3. Login with correct credentials
        UserLoginRequest loginReq = new UserLoginRequest("jane_doe", "my_secure_password_123");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.type", is("Bearer")));

        // 4. Login with incorrect credentials should fail (401 Unauthorized)
        UserLoginRequest wrongLoginReq = new UserLoginRequest("jane_doe", "wrong_password");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(wrongLoginReq)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error", is("Unauthorized")));
    }

    @Test
    public void testBookmarkCrudFlowAndSecurity() throws Exception {
        // Register & login to get token
        UserRegistrationRequest regReq = new UserRegistrationRequest("owner_user", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated());

        UserLoginRequest loginReq = new UserLoginRequest("owner_user", "password123");
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TokenResponse tokenResponse = objectMapper.readValue(loginResponse, TokenResponse.class);
        String token = "Bearer " + tokenResponse.getToken();

        // 1. Create a bookmark
        BookmarkRequest bookmarkReq = new BookmarkRequest(
                "https://spring.io",
                "Spring Framework",
                "Great framework",
                Arrays.asList("java", "tech")
        );

        String createResponse = mockMvc.perform(post("/api/bookmarks")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookmarkReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Spring Framework")))
                .andExpect(jsonPath("$.url", is("https://spring.io")))
                .andExpect(jsonPath("$.notes", is("Great framework")))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andReturn().getResponse().getContentAsString();

        BookmarkResponse createdBookmark = objectMapper.readValue(createResponse, BookmarkResponse.class);
        Long bookmarkId = createdBookmark.getId();

        // 2. Read the bookmark by ID
        mockMvc.perform(get("/api/bookmarks/" + bookmarkId)
                .header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookmarkId.intValue())))
                .andExpect(jsonPath("$.title", is("Spring Framework")));

        // 3. Update the bookmark
        BookmarkRequest updateReq = new BookmarkRequest(
                "https://spring.io/projects/spring-boot",
                "Spring Boot Project",
                "Updated notes",
                Collections.singletonList("springboot")
        );

        mockMvc.perform(put("/api/bookmarks/" + bookmarkId)
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookmarkId.intValue())))
                .andExpect(jsonPath("$.title", is("Spring Boot Project")))
                .andExpect(jsonPath("$.url", is("https://spring.io/projects/spring-boot")))
                .andExpect(jsonPath("$.notes", is("Updated notes")))
                .andExpect(jsonPath("$.tags", hasSize(1)))
                .andExpect(jsonPath("$.tags[0].name", is("springboot")));

        // 4. Test security: Another user should not access this bookmark
        // Register another user
        UserRegistrationRequest otherReg = new UserRegistrationRequest("other_user", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherReg)))
                .andExpect(status().isCreated());

        UserLoginRequest otherLogin = new UserLoginRequest("other_user", "password123");
        String otherLoginResp = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(otherLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String otherToken = "Bearer " + objectMapper.readValue(otherLoginResp, TokenResponse.class).getToken();

        // Other user tries to GET bookmark by ID -> 403 Forbidden
        mockMvc.perform(get("/api/bookmarks/" + bookmarkId)
                .header("Authorization", otherToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error", is("Forbidden")));

        // Other user tries to PUT bookmark by ID -> 403 Forbidden
        mockMvc.perform(put("/api/bookmarks/" + bookmarkId)
                .header("Authorization", otherToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isForbidden());

        // Other user tries to DELETE bookmark by ID -> 403 Forbidden
        mockMvc.perform(delete("/api/bookmarks/" + bookmarkId)
                .header("Authorization", otherToken))
                .andExpect(status().isForbidden());

        // 5. Delete bookmark
        mockMvc.perform(delete("/api/bookmarks/" + bookmarkId)
                .header("Authorization", token))
                .andExpect(status().isNoContent());

        // Verify it is deleted
        mockMvc.perform(get("/api/bookmarks/" + bookmarkId)
                .header("Authorization", token))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testSearchAndFilterBookmarks() throws Exception {
        // Register & login
        UserRegistrationRequest regReq = new UserRegistrationRequest("search_user", "password123");
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(regReq)))
                .andExpect(status().isCreated());

        UserLoginRequest loginReq = new UserLoginRequest("search_user", "password123");
        String loginResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String token = "Bearer " + objectMapper.readValue(loginResponse, TokenResponse.class).getToken();

        // Create 3 bookmarks
        BookmarkRequest b1 = new BookmarkRequest("https://google.com", "Google Search Engine", "Notes", Arrays.asList("search", "google"));
        BookmarkRequest b2 = new BookmarkRequest("https://github.com", "GitHub Code Repository", "Notes", Arrays.asList("git", "code"));
        BookmarkRequest b3 = new BookmarkRequest("https://wikipedia.org", "Wikipedia Online Encyclopedia", "Notes", Arrays.asList("knowledge", "search"));

        mockMvc.perform(post("/api/bookmarks").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(b1))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/bookmarks").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(b2))).andExpect(status().isCreated());
        mockMvc.perform(post("/api/bookmarks").header("Authorization", token).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(b3))).andExpect(status().isCreated());

        // 1. List all bookmarks without filter -> expect 3 bookmarks, newest-first
        mockMvc.perform(get("/api/bookmarks").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].title", is("Wikipedia Online Encyclopedia")))
                .andExpect(jsonPath("$[1].title", is("GitHub Code Repository")))
                .andExpect(jsonPath("$[2].title", is("Google Search Engine")));

        // 2. Search by title containing "Repository"
        mockMvc.perform(get("/api/bookmarks?title=Repository").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("GitHub Code Repository")));

        // 3. Search by title containing "engine" (case-insensitive)
        mockMvc.perform(get("/api/bookmarks?title=engine").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Google Search Engine")));

        // 4. Filter by tag "search"
        mockMvc.perform(get("/api/bookmarks?tags=search").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Wikipedia Online Encyclopedia")))
                .andExpect(jsonPath("$[1].title", is("Google Search Engine")));

        // 5. Filter by multiple tags: "search" and "google" (matches Google bookmark only)
        mockMvc.perform(get("/api/bookmarks?tags=search,google").header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Google Search Engine")));
    }
}
