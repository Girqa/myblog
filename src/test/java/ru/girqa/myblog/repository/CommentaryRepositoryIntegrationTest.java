package ru.girqa.myblog.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;
import ru.girqa.myblog.model.domain.Commentary;
import ru.girqa.myblog.repository.common.PostgresBaseIntegrationTest;
import ru.girqa.myblog.repository.jdbc.CommentaryJdbcRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = {CommentaryJdbcRepository.class})
class CommentaryRepositoryIntegrationTest extends PostgresBaseIntegrationTest {

    @Autowired
    CommentaryRepository repository;

    static final Long POST_ID = 5L;

    static final Long COMMENTARY_ID = 12L;

    static final String CLEAR = "truncate table posts, commentaries cascade;";

    static final String SET_POSTS_ID_SEQ = "select setval('posts_id_seq', 5, false);";

    static final String SET_COMMENTARIES_ID_SEQ = "select setval('commentaries_id_seq', 12, false);";

    static final String CREATE_POST = """
            insert into posts(title, image, post_text, likes)
            values ('post title', 'DEADBEEF'::bytea, 'some text', 10);
            """;

    @Nested
    @SqlGroup({
            @Sql(statements = {CLEAR, SET_POSTS_ID_SEQ, SET_COMMENTARIES_ID_SEQ, CREATE_POST}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(statements = {SET_POSTS_ID_SEQ, SET_COMMENTARIES_ID_SEQ}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class CommentarySearchTests {

        static final int COMMENTARIES_COUNT = 10;

        static final List<Commentary> COMMENTARIES = new ArrayList<>();

        @BeforeEach
        void setUpCommentaries() {
            COMMENTARIES.clear();
            for (int i = 0; i < COMMENTARIES_COUNT; ++i) {
                Commentary saved = repository.save(Commentary.builder()
                        .postId(POST_ID)
                        .text("TEXT_%d".formatted(i))
                        .build()
                );
                COMMENTARIES.add(saved);
            }
        }

        @Test
        void shouldFindAllByOneCommentariesInDb() {
            for (Commentary commentary: COMMENTARIES) {
                Optional<Commentary> dbCommentary = repository.findById(commentary.getId());
                assertTrue(dbCommentary.isPresent());
                assertThat(dbCommentary.get())
                        .usingRecursiveComparison()
                        .isEqualTo(commentary);
            }
        }

        @Test
        void shouldFindAllCommentariesBindedToPost() {
            List<Commentary> dbCommentaries = repository.findByPostId(POST_ID);
            assertThat(dbCommentaries)
                    .hasSize(COMMENTARIES.size())
                    .usingRecursiveFieldByFieldElementComparator()
                    .containsExactlyElementsOf(COMMENTARIES);
        }

        @Test
        void shouldCountAllCommentariesOfPost() {
            Long countByPostId = repository.countByPostId(POST_ID);
            assertEquals(COMMENTARIES.size(), countByPostId);
        }

    }

    @Nested
    @SqlGroup({
            @Sql(statements = {CLEAR, SET_POSTS_ID_SEQ, SET_COMMENTARIES_ID_SEQ}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(statements = {SET_POSTS_ID_SEQ, SET_COMMENTARIES_ID_SEQ}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class CommentaryCreationTests {

        Commentary commentary = Commentary.builder()
                .id(null)
                .postId(POST_ID)
                .text("Some comment text")
                .build();

        @Test
        void shouldNotSaveCommentaryIfPostNotPresent() {
            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> repository.save(commentary)
            );
        }

        @Test
        @Sql(statements = CREATE_POST)
        void shouldSaveCommentaryWhenPostPresent() {
            Commentary saved = repository.save(commentary);
            assertNotNull(saved);
            assertNotNull(saved.getId());
            assertEquals(COMMENTARY_ID, saved.getId());
        }
    }

    @Nested
    @SqlGroup({
            @Sql(statements = {CLEAR, SET_POSTS_ID_SEQ, SET_COMMENTARIES_ID_SEQ, CREATE_POST}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
            @Sql(statements = {SET_POSTS_ID_SEQ, SET_COMMENTARIES_ID_SEQ}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    })
    class CommentaryUpdateTests {

        Commentary sourceCommentary = Commentary.builder()
                .id(null)
                .postId(POST_ID)
                .text("Source text")
                .build();

        @BeforeEach
        void setUpCommentary() {
            sourceCommentary = repository.save(sourceCommentary);
        }

        @Test
        void shouldDeleteCommentaryIfPresent() {
            assertTrue(repository.findById(COMMENTARY_ID).isPresent());
            assertDoesNotThrow(() -> repository.delete(sourceCommentary.getId()));
            assertTrue(repository.findById(COMMENTARY_ID).isEmpty());
        }

        @Test
        void shouldDoNothingOnDeleteNotPresentCommentary() {
            assertTrue(repository.findById(COMMENTARY_ID - 1).isEmpty());
            assertDoesNotThrow(() -> repository.delete(COMMENTARY_ID));
        }

        @Test
        void shouldUpdateCommentaryText() {
            Commentary changedCommentary = sourceCommentary.toBuilder()
                    .text("Changed text")
                    .build();

            repository.update(changedCommentary);

            Optional<Commentary> dbCommentary = repository.findById(changedCommentary.getId());
            assertTrue(dbCommentary.isPresent());
            assertThat(dbCommentary.get())
                    .usingRecursiveComparison()
                    .isEqualTo(changedCommentary);
        }

    }

}