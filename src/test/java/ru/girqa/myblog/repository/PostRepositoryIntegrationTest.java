package ru.girqa.myblog.repository;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import ru.girqa.myblog.model.domain.PageRequest;
import ru.girqa.myblog.model.domain.post.Post;
import ru.girqa.myblog.model.domain.post.PostPreview;
import ru.girqa.myblog.model.domain.post.PostsPage;
import ru.girqa.myblog.repository.common.PostgresBaseIntegrationTest;
import ru.girqa.myblog.repository.jdbc.PostJdbcRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = {PostJdbcRepository.class})
public class PostRepositoryIntegrationTest extends PostgresBaseIntegrationTest {

    @Autowired
    PostRepository postRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    static final Long FIRST_POST_ID = 4L;

    static final String CLEAR = "truncate table posts, tags cascade;";

    static final String SET_POSTS_ID_SEQ = "select setval('posts_id_seq', 4, false);";

    @Nested
    @Sql(statements = {CLEAR, SET_POSTS_ID_SEQ}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    class PostSaveTests {

        @Test
        @SneakyThrows
        void shouldSavePost() {
            Post post = Post.builder()
                    .title("Post")
                    .likes(10)
                    .text("Text")
                    .build();

            Post saved = postRepository.save(post);
            assertEquals(FIRST_POST_ID, saved.getId());
            assertEquals(post.getTitle(), saved.getTitle());
            assertEquals(post.getImage(), saved.getImage());
            assertEquals(post.getLikes(), saved.getLikes());
            assertEquals(post.getText(), saved.getText());
        }

        @Test
        @SneakyThrows
        void shouldNotSaveNotFullPost() {
            Post post = Post.builder()
                    .title("Hello")
                    .build();
            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> postRepository.save(post)
            );
        }
    }

    @Nested
    @Sql(statements = {CLEAR, SET_POSTS_ID_SEQ}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    class PostUpdateTests {

        @Test
        void shouldUpdatePresentPost() {
            jdbcTemplate.update("""
                    insert into posts(title, post_text, likes)
                    values ('post1', 't1', 5)
                    """);

            Post updated = Post.builder()
                    .id(FIRST_POST_ID)
                    .title("New title")
                    .text("NewText")
                    .build();

            assertDoesNotThrow(() -> postRepository.update(updated));

            Optional<Post> dbPost = postRepository.findById(updated.getId());
            assertTrue(dbPost.isPresent());
            assertThat(dbPost.get())
                    .usingRecursiveComparison()
                    .isEqualTo(updated.toBuilder()
                            .likes(5) //this field should not update
                            .build()
                    );
        }

        @Test
        void shouldNotUpdateNotPresentPost() {
            Post updated = Post.builder()
                    .id(FIRST_POST_ID)
                    .title("New title")
                    .text("NewText")
                    .build();

            assertDoesNotThrow(() -> postRepository.update(updated));
            Optional<Post> dbPost = postRepository.findById(updated.getId());
            assertTrue(dbPost.isEmpty());
        }

        @Test
        void shouldIncrementLikes() {
            jdbcTemplate.update("""
                    insert into posts(title, post_text, likes)
                    values ('title', 'text', 2)
                    """);

            Post expected = Post.builder()
                    .id(FIRST_POST_ID)
                    .title("title")
                    .text("text")
                    .likes(2)
                    .build();

            Integer currentLikes = assertDoesNotThrow(() -> postRepository.incrementLikes(FIRST_POST_ID));
            assertEquals(expected.getLikes() + 1, currentLikes);

            Optional<Post> dbPost = postRepository.findById(FIRST_POST_ID);
            assertTrue(dbPost.isPresent());
            assertThat(dbPost.get())
                    .usingRecursiveComparison()
                    .isEqualTo(expected.toBuilder()
                            .likes(expected.getLikes() + 1)
                            .build()
                    );
        }

        @Test
        void shouldThrowIfIncrementLikesForNotPresentPost() {
            assertThrows(
                    EmptyResultDataAccessException.class,
                    () -> postRepository.incrementLikes(FIRST_POST_ID + 3)
            );
        }
    }

    @Nested
    @Sql(statements = {CLEAR, SET_POSTS_ID_SEQ}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    class PostDeleteTests {

        @Test
        void shouldDeletePresentPost() {
            jdbcTemplate.update("""
                    insert into posts(title, post_text, likes)
                    values ('title', 'text', 2)
                    """);

            assertDoesNotThrow(() -> postRepository.deleteById(FIRST_POST_ID));

            Optional<Post> dbPost = postRepository.findById(FIRST_POST_ID);
            assertTrue(dbPost.isEmpty());
        }

    }

    @Nested
    @Sql(statements = {CLEAR, SET_POSTS_ID_SEQ}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    class PostSearchTests {

        @BeforeEach
        void setUpPosts() {
            jdbcTemplate.update("""
                    insert into posts(title, post_text)
                    values ('post1', 't1'), ('post2', 't2')
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
                    () -> assertEquals(0, dbPost.getLikes())
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
                    insert into posts(title, post_text, likes)
                    values ('post1', 't1', 5), ('post2', 't2', 4)
                    """);

            posts = List.of(
                    Post.builder()
                            .id(FIRST_POST_ID)
                            .title("post1")
                            .text("t1")
                            .likes(5)
                            .build(),
                    Post.builder()
                            .id(FIRST_POST_ID + 1)
                            .title("post2")
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
                    () -> assertEquals(post.getLikes(), preview.getLikes())
            );
        }

    }

}


