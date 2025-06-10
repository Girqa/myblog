package ru.girqa.myblog.repository;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import ru.girqa.myblog.model.domain.post.Image;
import ru.girqa.myblog.repository.common.PostgresTestConfiguration;
import ru.girqa.myblog.repository.jdbc.ImageJdbcRepository;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Import(PostgresTestConfiguration.class)
@SpringBootTest(classes = ImageJdbcRepository.class)
class ImageRepositoryIntegrationTest {

    @Autowired
    ImageRepository imageRepository;

    static final Long POST_ID = 11L;

    static final String CLEAR = "truncate table posts, images cascade;";

    static final String CREATE_POST = """
            select setval('posts_id_seq', 11, false);
            insert into posts(title, post_text, likes)
            values ('Title', 'lorem', 22);
            """;

    static final String CREATE_IMAGE = """
            insert into images(post_id, image_name, image_data, image_size)
            values (11, 'Image Name', convert_to('ehal greka', 'UTF8'), 10);
            """;

    final Image dbImage = Image.builder()
            .postId(POST_ID)
            .name("Image Name")
            .data("ehal greka".getBytes(StandardCharsets.UTF_8))
            .size(10)
            .build();

    @Nested
    @Sql(statements = {CREATE_POST, CREATE_IMAGE}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAR, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class FindTests {

        @Test
        void shouldFindPresentImage() {
            Optional<Image> image = assertDoesNotThrow(() -> imageRepository.findByPostId(POST_ID));
            assertTrue(image.isPresent());
            assertThat(image.get())
                    .usingRecursiveComparison()
                    .isEqualTo(dbImage);
        }

        @Test
        void shouldNotFindNotPresentImage() {
            Optional<Image> image = assertDoesNotThrow(() -> imageRepository.findByPostId(POST_ID + 222));
            assertTrue(image.isEmpty());
        }
    }

    @Nested
    @Sql(statements = CREATE_POST, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAR, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class SaveTests {

        @Test
        void shouldSaveImageIfPostPresent() {
            final Image given = Image.builder()
                    .postId(POST_ID)
                    .name("Image")
                    .data(new byte[]{'a', 'b', 'c', 'd'})
                    .size(4)
                    .build();

            assertDoesNotThrow(() -> imageRepository.save(given));

            Optional<Image> dbImage = imageRepository.findByPostId(POST_ID);
            assertTrue(dbImage.isPresent());
            assertThat(dbImage.get())
                    .usingRecursiveComparison()
                    .isEqualTo(given);
        }

        @Test
        void shouldNotSaveImageIfPostIsNotPresent() {
            final Image given = Image.builder()
                    .postId(POST_ID + 10)
                    .name("black")
                    .data(new byte[]{'1', '2', '3'})
                    .size(3)
                    .build();

            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> imageRepository.save(given)
            );

            Optional<Image> dbImage = imageRepository.findByPostId(POST_ID);
            assertTrue(dbImage.isEmpty());
        }

        @Test
        void shouldNotSaveImageIfAlreadyPresentForPost() {
            final Image presentImage = Image.builder()
                    .postId(POST_ID)
                    .name("name")
                    .data(new byte[]{'2', 'a', '3'})
                    .size(3)
                    .build();

            assertDoesNotThrow(() -> imageRepository.save(presentImage));
            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> imageRepository.save(presentImage)
            );
        }
    }

    @Nested
    @Sql(statements = {CREATE_POST, CREATE_IMAGE}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(statements = CLEAR, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    class UpdateTests {

        final Image dbImage = Image.builder()
                .postId(POST_ID)
                .name("Image Name")
                .data(new byte[]{'e', 'h', 'a', 'l', 'g', 'r', 'e', 'k', 'a'})
                .size(9)
                .build();

        @Test
        void shouldUpdatePresentImage() {
            Image updated = dbImage.toBuilder()
                    .name("new name")
                    .data(new byte[]{'n', 'e', 'w'})
                    .size(3)
                    .build();

            assertDoesNotThrow(() -> imageRepository.update(updated));

            Optional<Image> dbImageUpdated = imageRepository.findByPostId(POST_ID);
            assertTrue(dbImageUpdated.isPresent());
            assertThat(dbImageUpdated.get())
                    .usingRecursiveComparison()
                    .isEqualTo(updated);
        }

        @Test
        void shouldNotUpdateNotPresentImage() {
            Image updated = dbImage.toBuilder()
                    .postId(POST_ID + 11)
                    .build();

            assertDoesNotThrow(() -> imageRepository.update(updated));

            Optional<Image> notFoundUpdate = imageRepository.findByPostId(updated.getPostId());
            assertTrue(notFoundUpdate.isEmpty());
        }
    }

}