package ru.girqa.myblog.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import ru.girqa.myblog.model.domain.Tag;
import ru.girqa.myblog.repository.common.PostgresBaseIntegrationTest;
import ru.girqa.myblog.repository.jdbc.TagJdbcRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = {TagJdbcRepository.class})
class TagRepositoryIntegrationTest extends PostgresBaseIntegrationTest {

    @Autowired
    TagRepository tagRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    static final Long FIRST_TAG_ID = 3L;

    static final Long POST_ID = 11L;

    static final String CLEAR_TAGS_ID_SEQ = "select setval('tags_id_seq', 3, false);";

    static final String CLEAR = "truncate table posts, tags cascade;";

    static final String CREATE_POST = """
            select setval('posts_id_seq', 11, false);
            insert into posts(title, image, post_text, likes)
            values ('Post', 'AAAAAAAA'::bytea, 'Lmao', 5);
            """;

    @Nested
    @Sql(statements = {CLEAR, CLEAR_TAGS_ID_SEQ}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    class TagsMergeTests {

        @Test
        void shouldMergeAllTags() {
            List<Tag> givenTags = List.of(
                    Tag.builder()
                            .name("tag1")
                            .build(),
                    Tag.builder()
                            .name("tag2")
                            .build()
            );

            List<Tag> merged = tagRepository.merge(givenTags);

            assertEquals(givenTags.size(), merged.size());
            for (int i = 0; i < merged.size(); ++i) {
                long expectedId = i + FIRST_TAG_ID;
                Tag mergedTag = merged.get(i);
                Tag givenTag = givenTags.get(i);

                assertAll(
                        () -> assertEquals(expectedId, mergedTag.getId()),
                        () -> assertEquals(givenTag.getName(), mergedTag.getName())
                );
            }
        }

        @Test
        void shouldDoNothingOnEmptyTags() {
            assertDoesNotThrow(() -> {
                List<Tag> merged = tagRepository.merge(List.of());
                assertTrue(merged.isEmpty());
            });
        }

        @Test
        void shouldMergeFewOfGivenAndGetPresentTagFromDb() {
            jdbcTemplate.update("insert into tags(tag_name) values ('Present tag')");

            Tag presentTag = Tag.builder()
                    .name("Present tag")
                    .build();

            List<Tag> givenTags = List.of(
                    presentTag,
                    Tag.builder()
                            .name("New tag")
                            .build()
            );

            List<Tag> mergedTags = tagRepository.merge(givenTags);
            assertEquals(givenTags.size(), mergedTags.size());

            Tag mergedTagWithPresentName = mergedTags.getFirst();
            assertEquals(FIRST_TAG_ID, mergedTagWithPresentName.getId());
            assertEquals(presentTag.getName(), mergedTagWithPresentName.getName());

            Tag newTag = mergedTags.getLast();
            assertEquals(FIRST_TAG_ID + 2, newTag.getId());  // present tag should conflict with existing but id seq will be incremented whatever
            assertEquals(givenTags.getLast().getName(), newTag.getName());
        }
    }

    @Nested
    @Sql(statements = {CLEAR, CLEAR_TAGS_ID_SEQ}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CREATE_POST, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    class TagSearchByPostTests {

        @BeforeEach
        void setUpTags() {
            jdbcTemplate.update("""
                    insert into tags(tag_name)
                    values ('Tag 1'), ('Tag 2')
                    """);
            jdbcTemplate.update("""
                    insert into post_tags(post_id, tag_id)
                    values (11, 3), (11, 4)
                    """);
        }

        @Test
        void shouldNotFindTags() {
            List<Tag> tags = tagRepository.findByPostId(POST_ID + 5);
            assertTrue(tags.isEmpty());
        }

        @Test
        void shouldFindTagsOfPost() {
            List<Tag> tags = tagRepository.findByPostId(POST_ID);
            assertEquals(2, tags.size());

            Tag firstTag = tags.getFirst();
            assertEquals(FIRST_TAG_ID, firstTag.getId());
            assertEquals("Tag 1", firstTag.getName());

            Tag secondTag = tags.getLast();
            assertEquals(FIRST_TAG_ID + 1, secondTag.getId());
            assertEquals("Tag 2", secondTag.getName());
        }
    }

    @Nested
    @Sql(statements = {CLEAR, CLEAR_TAGS_ID_SEQ}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CREATE_POST, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    class TagBindingTests {

        @Test
        void shouldNotBindNotPresentTag() {
            Tag notPresent = Tag.builder()
                    .id(1L)
                    .name("NotPresent")
                    .build();
            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> tagRepository.bindTagsToPost(POST_ID, List.of(notPresent))
            );
        }

        @Test
        void shouldNotBindPresentTagToNotPresentPost() {
            jdbcTemplate.update("insert into tags(tag_name) values ('Present tag')");
            List<Tag> presentTags = List.of(
                    Tag.builder()
                            .id(FIRST_TAG_ID)
                            .name("Present Tag")
                            .build()
            );
            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> tagRepository.bindTagsToPost(POST_ID + 1, presentTags)
            );
        }

        @Test
        void shouldBindPresentTagsToPresentPost() {
            jdbcTemplate.update("insert into tags(tag_name) values ('Present Tag 1')");
            jdbcTemplate.update("insert into tags(tag_name) values ('Present Tag 2')");
            List<Tag> presentTags = List.of(
                    Tag.builder()
                            .id(FIRST_TAG_ID)
                            .name("Present Tag 1")
                            .build(),
                    Tag.builder()
                            .id(FIRST_TAG_ID + 1)
                            .name("Present Tag 2")
                            .build()
            );
            assertDoesNotThrow(
                    () -> tagRepository.bindTagsToPost(POST_ID, presentTags)
            );

            record TagPostLink(long postId, long tagId) {
            }
            List<TagPostLink> links = jdbcTemplate.query(
                    "select post_id, tag_id from post_tags",
                    (rs, n) -> new TagPostLink(rs.getLong("post_id"), rs.getLong("tag_id"))
            );
            assertEquals(presentTags.size(), links.size());
            assertEquals(POST_ID, links.getFirst().postId);
            assertEquals(POST_ID, links.getLast().postId);
            assertEquals(FIRST_TAG_ID, links.getFirst().tagId);
            assertEquals(FIRST_TAG_ID + 1, links.getLast().tagId);
        }

    }
}