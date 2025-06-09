package ru.girqa.myblog.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import ru.girqa.myblog.model.domain.PageRequest;
import ru.girqa.myblog.model.domain.Tag;
import ru.girqa.myblog.model.domain.post.Image;
import ru.girqa.myblog.model.domain.post.Post;
import ru.girqa.myblog.model.domain.post.PostPreview;
import ru.girqa.myblog.model.domain.post.PostsPage;
import ru.girqa.myblog.model.dto.commentary.CommentaryDto;
import ru.girqa.myblog.model.dto.post.CreatePostDto;
import ru.girqa.myblog.model.dto.post.PostDto;
import ru.girqa.myblog.model.dto.post.PostPreviewDto;
import ru.girqa.myblog.model.dto.post.UpdatePostDto;
import ru.girqa.myblog.model.mapper.PostMapper;
import ru.girqa.myblog.service.PostsService;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class PostsControllerTest extends BaseControllerTest {

    @Autowired
    PostsService postsServiceMock;

    @Autowired
    PostMapper postMapperMock;

    @BeforeEach
    void setUpMocks() {
        reset(postsServiceMock, postMapperMock);
    }

    @Nested
    class AllPostsPageTests {

        List<PostPreview> preparedPosts = List.of(
                PostPreview.builder()
                        .id(2L)
                        .title("Title 1")
                        .text("Text 1")
                        .likes(5)
                        .comments(2L)
                        .tags(List.of(
                                Tag.builder()
                                        .id(3L)
                                        .name("T1")
                                        .build(),
                                Tag.builder()
                                        .id(4L)
                                        .name("T2")
                                        .build()
                        ))
                        .build(),
                PostPreview.builder()
                        .id(3L)
                        .title("Title 2")
                        .text("Text 2")
                        .likes(8)
                        .comments(1L)
                        .tags(List.of(
                                Tag.builder()
                                        .id(5L)
                                        .name("T1")
                                        .build()
                        ))
                        .build()
        );

        static Stream<Arguments> providedParams() {
            return Stream.of(
                    Arguments.of(null, 10, null, null),
                    Arguments.of(5, 17, null, null),
                    Arguments.of(null, 10, 20, null),
                    Arguments.of(null, 1, null, "T1"),
                    Arguments.of(null, 5, 50, "T1"),
                    Arguments.of(7, 4, null, "T1"),
                    Arguments.of(7, 9, 10, null),
                    Arguments.of(2, 22, 20, "T")
            );
        }

        @SneakyThrows
        @ParameterizedTest
        @MethodSource("providedParams")
        void shouldReturnPostsPageViewForAllParamsFilled(
                Integer page,
                int totalPages,
                Integer postsPerPage,
                String tag
        ) {
            final int PAGE = page == null ? PostsController.DEFAULT_PAGE : page;
            final int POSTS_PER_PAGE = postsPerPage == null ? PostsController.DEFAULT_POSTS_PER_PAGE : postsPerPage;
            prepareMocks(PAGE, totalPages, POSTS_PER_PAGE, tag);

            Map<String, String> params = new HashMap<>();
            if (page != null) params.put("page", String.valueOf(page));
            if (postsPerPage != null) params.put("postsPerPage", String.valueOf(postsPerPage));
            if (tag != null) params.put("tag", tag);

            mockMvc.perform(get("/posts")
                            .queryParams(MultiValueMap.fromSingleValue(params)))
                    .andExpect(verifyModel(PAGE, totalPages, POSTS_PER_PAGE, tag));

            verify(postsServiceMock, times(1))
                    .getPostsPage(PageRequest.builder()
                            .page(PAGE)
                            .posts(POSTS_PER_PAGE)
                            .targetTag(tag)
                            .build());
        }

        private void prepareMocks(Integer page,
                                  Integer totalPages,
                                  Integer postsPerPage,
                                  String tag) {
            when(postsServiceMock.getPostsPage(any()))
                    .thenReturn(PostsPage.builder()
                            .page(page)
                            .totalPages(totalPages)
                            .postsPerPage(postsPerPage)
                            .targetTag(tag)
                            .posts(preparedPosts)
                            .build()
                    );

            when(postMapperMock.toDto(any(PostPreview.class)))
                    .thenReturn(new PostPreviewDto(
                            2L, "Title 1", "Text 1", 5, 2,
                            List.of("T1", "T2")
                    ))
                    .thenReturn(
                            new PostPreviewDto(
                                    3L, "Title 2", "Text 2", 8, 1,
                                    List.of("T1")
                            )
                    );
        }

        private ResultMatcher verifyModel(Integer page,
                                          Integer totalPages,
                                          Integer posts,
                                          String tag) {
            return mvcResult -> {
                status().isOk().match(mvcResult);
                content().contentType("text/html;charset=UTF-8").match(mvcResult);
                view().name("all-posts").match(mvcResult);

                model().attributeExists(
                        "newPost",
                        "posts",
                        "page",
                        "postsPerPage",
                        "availablePostsPerPage",
                        "totalPages",
                        "searchTag"
                ).match(mvcResult);

                // controls
                xpath("//input[@name='tag'][@value='%s']", tag == null ? "" : tag)
                        .exists()
                        .match(mvcResult);

                xpath("//div[@class='block']/div[contains(@class, 'btn')][text()='%d/%d']", page, totalPages)
                        .exists()
                        .match(mvcResult);

                xpath("//select[@name='postsPerPage']/option[@selected][@value='%d']", posts)
                        .exists()
                        .match(mvcResult);

                xpath("//div[@id='postModal']")
                        .exists()
                        .match(mvcResult);

                // posts

                xpath("//article[@class='post']").number(2.0);
                for (int i = 0; i < preparedPosts.size(); ++i) {
                    PostPreview post = preparedPosts.get(i);
                    final String BASE_TAG = "//article[@class='post'][%d]".formatted(i + 1);
                    xpath(BASE_TAG + "//a[contains(@href, '/posts/post/%d')]", post.getId())
                            .exists()
                            .match(mvcResult);
                    xpath(BASE_TAG + "//a/h2[@class='post_title'][text()='%s']", post.getTitle())
                            .exists()
                            .match(mvcResult);
                    xpath(BASE_TAG + "//div[@class='post-likes'][text()='%d']", post.getLikes())
                            .exists()
                            .match(mvcResult);
                    xpath(BASE_TAG + "//div[@class='post-comments'][text()='%d']", post.getComments())
                            .exists()
                            .match(mvcResult);
                    xpath(BASE_TAG + "//div[@class='tags-block']/span")
                            .nodeCount(post.getTags().size())
                            .match(mvcResult);
                    for (int j = 0; j < post.getTags().size(); ++j) {
                        xpath(BASE_TAG + "//div[@class=tags-block]/div[%d]/span[@class='tag'][text()='%s']",
                                j + 1, post.getTags().get(j));
                    }
                    xpath(BASE_TAG + "//img[contains(@src, '/posts/post/%d/image')]", post.getId())
                            .exists()
                            .match(mvcResult);
                    xpath(BASE_TAG + "//p[@class='post_text'][text()='%s']", post.getText())
                            .exists()
                            .match(mvcResult);
                }
            };
        }
    }

    @Nested
    class PostTests {

        @Test
        @SneakyThrows
        void shouldReturnPostPage() {
            final Long POST_ID = 3L;

            final PostDto post = PostDto.builder()
                    .id(POST_ID)
                    .title("Title")
                    .text("Row1\nRow2")
                    .commentaries(List.of(
                            new CommentaryDto(6L, POST_ID, "C1"),
                            new CommentaryDto(7L, POST_ID, "C2")
                    ))
                    .tags(List.of("T1", "T2"))
                    .likes(11L)
                    .build();

            when(postsServiceMock.findPost(POST_ID))
                    .thenReturn(Post.builder()
                            .id(POST_ID)
                            .build());

            when(postMapperMock.toDto(any(Post.class)))
                    .thenReturn(post);

            when(postMapperMock.toUpdateDto(any(Post.class)))
                    .thenReturn(new UpdatePostDto(
                            "Title", mock(MultipartFile.class), "Row1\nRow2", List.of("T1", "T2")
                    ));

            mockMvc.perform(get("/posts/post/{id}", POST_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("text/html;charset=UTF-8"))
                    .andExpect(view().name("post"))
                    .andExpect(model().attributeExists("post", "editPost", "createCommentary"))
                    .andExpect(xpath("//a[contains(@href, '/posts')]").exists())
                    .andExpect(xpath("//div[@id='postModal']").exists())
                    .andExpect(xpath("//h1[text()='%s']", post.getTitle()).exists())
                    .andExpect(xpath("//form[contains(@action, '/posts/post/%d')][@method='post']", post.getId()).exists())
                    .andExpect(xpath("//img[contains(@src, '/posts/post/%d/image')]", post.getId()).exists())
                    .andExpect(xpath("//div[contains(@class,'post-text')]").exists())
                    .andExpect(xpath("//div[contains(@class,'post-text')]/p").nodeCount(2))
                    .andExpect(xpath("//div[@class='tags-block']/span[@class='tag']").nodeCount(post.getTags().size()))
                    .andExpect(xpath("//span[@class='post-likes'][text()='%d']", post.getLikes()).exists())
                    .andExpect(xpath("//form[contains(@action, '/commentaries/commentary')]").exists())
                    .andExpect(xpath("//div[@class='comment']").nodeCount(post.getCommentaries().size()));
        }

        @Test
        @SneakyThrows
        void shouldCreatePost() {
            Post newPost = Post.builder()
                    .title("TTT")
                    .text("A\nB\nC")
                    .image(Image.builder()
                            .name("Photo")
                            .data("Hello!".getBytes(StandardCharsets.UTF_8))
                            .size("Hello!".getBytes(StandardCharsets.UTF_8).length)
                            .build()
                    )
                    .tags(List.of(
                            Tag.builder().name("T1").build(),
                            Tag.builder().name("T2").build(),
                            Tag.builder().name("T3").build()
                    ))
                    .build();
            when(postMapperMock.toDomain(any(CreatePostDto.class)))
                    .thenReturn(newPost);

            Map<String, List<String>> createPostParams = Map.of(
                    "title", List.of("TTT"),
                    "text", List.of("A\nB\nC"),
                    "tags", List.of("T1", "T2", "T3")
            );

            MockMultipartFile image = new MockMultipartFile("image", "Hello!".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(multipart(HttpMethod.POST, "/posts/post")
                            .file(image)
                            .params(MultiValueMap.fromMultiValue(createPostParams))
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/posts"));

            verify(postMapperMock, times(1))
                    .toDomain(new CreatePostDto("TTT", image, "A\nB\nC", List.of("T1", "T2", "T3")));

            verify(postsServiceMock, times(1))
                    .create(newPost);
        }

        @Test
        @SneakyThrows
        void shouldUpdatePost() {
            final Long POST_ID = 5L;

            Post updatedPost = Post.builder()
                    .title("Best title")
                    .text("Some text")
                    .image(Image.builder()
                            .name("Perfect image")
                            .data("Content".getBytes(StandardCharsets.UTF_8))
                            .size("Content".getBytes(StandardCharsets.UTF_8).length)
                            .build())
                    .tags(List.of(
                            Tag.builder().name("D&D").build(),
                            Tag.builder().name("Blood rage").build(),
                            Tag.builder().name("Doom").build()
                    ))
                    .build();

            when(postMapperMock.toDomain(any(UpdatePostDto.class)))
                    .thenReturn(updatedPost);

            Map<String, List<String>> updatePostParams = Map.of(
                    "title", List.of("Best title"),
                    "text", List.of("Some text"),
                    "tags", List.of("D&D", "Blood rage", "Doom")
            );

            MockMultipartFile image = new MockMultipartFile("image", "Content".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(multipart(HttpMethod.POST, "/posts/post/{id}", POST_ID)
                            .file(image)
                            .params(MultiValueMap.fromMultiValue(updatePostParams))
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrlTemplate("/posts/post/{id}", POST_ID));

            verify(postMapperMock, times(1))
                    .toDomain(new UpdatePostDto(
                            "Best title", image, "Some text", List.of("D&D", "Blood rage", "Doom")));

            verify(postsServiceMock, times(1))
                    .update(updatedPost.toBuilder()
                            .id(POST_ID)
                            .build());
        }

        @Test
        @SneakyThrows
        void shouldDeletePost() {
            final Long POST_ID = 333L;

            mockMvc.perform(delete("/posts/post/{id}", POST_ID))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/posts"));

            verify(postsServiceMock, times(1))
                    .delete(POST_ID);
        }

        @Test
        @SneakyThrows
        void shouldRequestPostImage() {
            final Long POST_ID = 54L;

            when(postsServiceMock.getImage(POST_ID))
                    .thenReturn("Mama ya v dubae".getBytes(StandardCharsets.UTF_8));

            mockMvc.perform(get("/posts/post/{id}/image", POST_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.IMAGE_JPEG_VALUE))
                    .andExpect(content().bytes("Mama ya v dubae".getBytes(StandardCharsets.UTF_8)));
        }

        @Test
        @SneakyThrows
        void shouldIncrementPostLikes() {
            final Long POST_ID = 901L;

            when(postsServiceMock.incrementLikes(POST_ID))
                    .thenReturn(11);

            mockMvc.perform(post("/posts/post/{id}/like", POST_ID))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                    .andExpect(content().bytes("11".getBytes(StandardCharsets.UTF_8)));
        }
    }
}