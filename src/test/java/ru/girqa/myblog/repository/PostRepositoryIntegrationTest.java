package ru.girqa.myblog.repository;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import ru.girqa.myblog.model.domain.PageRequest;
import ru.girqa.myblog.model.domain.post.Post;
import ru.girqa.myblog.model.domain.post.PostPreview;
import ru.girqa.myblog.model.domain.post.PostsPage;
import ru.girqa.myblog.repository.common.PostgresBaseIntegrationTest;
import ru.girqa.myblog.repository.jdbc.PostJdbcRepository;

import javax.sql.rowset.serial.SerialBlob;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ContextConfiguration(classes = {PostJdbcRepository.class, PostRepositoryIntegrationTest.MockDependenciesConfiguration.class})
public class PostRepositoryIntegrationTest extends PostgresBaseIntegrationTest {

    @Autowired
    PostRepository postRepository;

    static TagRepository tagRepositoryMock = mock(TagRepository.class);

    static CommentaryRepository commentaryRepositoryMock = mock(CommentaryRepository.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    static final Long FIRST_POST_ID = 4L;

    static final String CLEAR = "truncate table posts, tags cascade;";

    static final String SET_POSTS_ID_SEQ = "select setval('posts_id_seq', 4, false);";

    @BeforeEach
    void setUpMocks() {
        reset(tagRepositoryMock, commentaryRepositoryMock);
    }

    @Nested
    @Sql(statements = {CLEAR, SET_POSTS_ID_SEQ}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    class PostSaveTests {

        @Test
        @SneakyThrows
        void shouldSavePost() {
            Post post = Post.builder()
                    .title("Post")
                    .image(new SerialBlob("Hello!".getBytes(StandardCharsets.UTF_8)))
                    .likes(10)
                    .text("Text")
                    .build();

            when(tagRepositoryMock.merge(anyList()))
                    .thenReturn(new ArrayList<>());

            Post saved = postRepository.save(post);
            assertEquals(FIRST_POST_ID, saved.getId());
            assertEquals(post.getTitle(), saved.getTitle());
            assertEquals(post.getImage(), saved.getImage());
            assertEquals(post.getLikes(), saved.getLikes());
            assertEquals(post.getText(), saved.getText());
        }
    }

    @Nested
    @Sql(statements = {CLEAR, SET_POSTS_ID_SEQ}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    class PostSearchTests {

        @BeforeEach
        void setUpPosts() {
            jdbcTemplate.update("""
                    insert into posts(title, image, post_text)
                    values ('post1', 'ABCDE'::bytea, 't1'), ('post2', 'ABCDE'::bytea, 't2')
                    """);
        }

        @Test
        @SneakyThrows
        void shouldFindPost() {
            Optional<Post> dbPostOp = postRepository.findById(FIRST_POST_ID);
            assertTrue(dbPostOp.isPresent());

            Post dbPost = dbPostOp.get();
            assertAll(
                    () -> assertEquals(FIRST_POST_ID, dbPost.getId()),
                    () -> assertEquals("post1", dbPost.getTitle()),
                    () -> assertEquals("t1", dbPost.getText()),
                    () -> assertEquals(0, dbPost.getLikes()),
                    () -> assertEquals(new SerialBlob("ABCDE".getBytes(StandardCharsets.UTF_8)), dbPost.getImage())
            );
        }

        @Test
        void shouldNotFindPost() {
            Optional<Post> dbPost = postRepository.findById(FIRST_POST_ID + 5);
            assertTrue(dbPost.isEmpty());
        }
    }

    @Nested
    @Sql(statements = {CLEAR, SET_POSTS_ID_SEQ}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    class PostPageTests {

        List<Post> posts;

        @BeforeEach
        @SneakyThrows
        void setUpPosts() {
            jdbcTemplate.update("""
                    insert into posts(title, image, post_text, likes)
                    values ('post1', 'ABCDE'::bytea, 't1', 5), ('post2', 'ABCDE'::bytea, 't2', 4)
                    """);

            posts = List.of(
                    Post.builder()
                            .id(FIRST_POST_ID)
                            .title("post1")
                            .image(new SerialBlob("ABCDE".getBytes(StandardCharsets.UTF_8)))
                            .text("t1")
                            .likes(5)
                            .build(),
                    Post.builder()
                            .id(FIRST_POST_ID + 1)
                            .title("post2")
                            .image(new SerialBlob("ABCDE".getBytes(StandardCharsets.UTF_8)))
                            .text("t2")
                            .likes(4)
                            .build()
            );
        }

        @Nested
        class WithoutTag {

            @Test
            @SneakyThrows
            void shouldFindOnePostAtPage() {
                PostsPage page = postRepository.findAllPaged(PageRequest.builder()
                        .page(1)
                        .posts(1)
                        .build()
                );

                assertAll(
                        () -> assertEquals(1, page.getPage()),
                        () -> assertEquals(1, page.getPostsPerPage()),
                        () -> assertEquals(2, page.getTotalPages()),
                        () -> assertEquals(1, page.getPosts().size())
                );

                PostPreview postPreview = page.getPosts().getFirst();
                assertPreviewCorrect(posts.getFirst(), postPreview);
            }

            @Test
            @SneakyThrows
            void shouldFindTwoPostsAtPage() {
                PostsPage page = postRepository.findAllPaged(PageRequest.builder()
                        .page(1)
                        .posts(2)
                        .build()
                );

                assertAll(
                        () -> assertEquals(1, page.getPage()),
                        () -> assertEquals(2, page.getPostsPerPage()),
                        () -> assertEquals(1, page.getTotalPages()),
                        () -> assertEquals(2, page.getPosts().size())
                );

                PostPreview firstPost = page.getPosts().getFirst();
                assertPreviewCorrect(posts.getFirst(), firstPost);

                PostPreview secondPost = page.getPosts().getLast();
                assertPreviewCorrect(posts.getLast(), secondPost);
            }

            @Test
            void shouldNotFindPosts() {
                jdbcTemplate.execute("truncate table posts cascade;");
                PostsPage page = postRepository.findAllPaged(PageRequest.builder()
                        .page(1)
                        .posts(5)
                        .build());

                assertAll(
                        () -> assertEquals(1, page.getPage()),
                        () -> assertEquals(5, page.getPostsPerPage()),
                        () -> assertEquals(0, page.getTotalPages()),
                        () -> assertEquals(0, page.getPosts().size())
                );
            }
        }

        @Nested
        class WithTag {

            @BeforeEach
            void setUpTags() {
                jdbcTemplate.execute("select setval('tags_id_seq', 4, false);");
                jdbcTemplate.update("insert into tags(tag_name) values ('t1'), ('t2')");
                jdbcTemplate.update("insert into post_tags(post_id, tag_id) values (4, 4), (5, 4), (5, 5)");
            }

            @Test
            void shouldFindAllPosts() {
                PostsPage page = postRepository.findAllPaged(PageRequest.builder()
                        .page(1)
                        .posts(5)
                        .targetTag("t1")
                        .build());

                assertEquals(2, page.getPosts().size());
                assertPreviewCorrect(posts.getFirst(), page.getPosts().getFirst());
                assertPreviewCorrect(posts.getLast(), page.getPosts().getLast());
            }

            @Test
            void shouldFindOnePost() {
                PostsPage page = postRepository.findAllPaged(PageRequest.builder()
                        .page(1)
                        .posts(5)
                        .targetTag("t2")
                        .build());

                assertEquals(1, page.getPosts().size());
                assertPreviewCorrect(posts.getLast(), page.getPosts().getFirst());
            }

            @Test
            void shouldNotFindPosts() {
                PostsPage page = postRepository.findAllPaged(PageRequest.builder()
                        .page(1)
                        .posts(3)
                        .targetTag("t3")
                        .build());

                assertTrue(page.getPosts().isEmpty());
            }
        }

        void assertPreviewCorrect(Post post, PostPreview preview) {
            assertAll(
                    () -> assertEquals(post.getId(), preview.getId()),
                    () -> assertEquals(post.getTitle(), preview.getTitle()),
                    () -> assertEquals(post.getText(), preview.getText()),
                    () -> assertEquals(post.getLikes(), preview.getLikes()),
                    () -> assertEquals(post.getImage(), preview.getImage())
            );
        }

    }

    @Configuration
    static class MockDependenciesConfiguration {

        @Bean
        TagRepository tagRepository() {
            return tagRepositoryMock;
        }

        @Bean
        CommentaryRepository commentaryRepository() {
            return commentaryRepositoryMock;
        }
    }

}


