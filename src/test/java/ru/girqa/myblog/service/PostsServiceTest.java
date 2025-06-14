package ru.girqa.myblog.service;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.girqa.myblog.exception.PostNotFoundException;
import ru.girqa.myblog.model.domain.Commentary;
import ru.girqa.myblog.model.domain.PageRequest;
import ru.girqa.myblog.model.domain.Tag;
import ru.girqa.myblog.model.domain.post.Image;
import ru.girqa.myblog.model.domain.post.Post;
import ru.girqa.myblog.model.domain.post.PostsPage;
import ru.girqa.myblog.repository.CommentaryRepository;
import ru.girqa.myblog.repository.ImageRepository;
import ru.girqa.myblog.repository.PostRepository;
import ru.girqa.myblog.repository.TagRepository;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostsServiceTest {

    @Mock
    PostRepository postRepositoryMock;

    @Mock
    ImageRepository imageRepositoryMock;

    @Mock
    TagRepository tagRepositoryMock;

    @Mock
    CommentaryRepository commentaryRepositoryMock;

    @InjectMocks
    PostsService postsService;

    @BeforeEach
    void setUpMocks() {
        reset(postRepositoryMock, tagRepositoryMock, commentaryRepositoryMock);
    }

    @Test
    @SneakyThrows
    void shouldCreatePost() {
        final Long POST_ID = 5L;
        final Image image = Image.builder()
                .size(10)
                .data(new byte[10])
                .build();

        Post post = Post.builder()
                .title("Title")
                .image(image)
                .text("Text")
                .tags(List.of(
                        Tag.builder()
                                .name("Linux")
                                .build(),
                        Tag.builder()
                                .name("Windows")
                                .build()
                ))
                .build();

        when(postRepositoryMock.save(any()))
                .thenReturn(post.toBuilder()
                        .id(POST_ID)
                        .build()
                );

        List<Tag> mergedTags = List.of(
                Tag.builder()
                        .id(4L)
                        .name("Linux")
                        .build(),
                Tag.builder()
                        .id(5L)
                        .name("Windows")
                        .build()
        );

        when(tagRepositoryMock.merge(anyList()))
                .thenReturn(mergedTags);

        Post saved = postsService.create(post);

        verify(postRepositoryMock, times(1))
                .save(post);

        verify(imageRepositoryMock, times(1))
                .save(image.toBuilder()
                        .postId(POST_ID)
                        .build()
                );

        verify(tagRepositoryMock, times(1))
                .merge(post.getTags());

        verify(tagRepositoryMock, times(1))
                .bindTagsToPost(saved.getId(), mergedTags);

        assertAll(
                () -> assertEquals(POST_ID, saved.getId()),
                () -> assertEquals(post.getTitle(), saved.getTitle()),
                () -> assertEquals(post.getText(), saved.getText()),
                () -> assertEquals(2, saved.getTags().size())
        );

        assertEquals(mergedTags, saved.getTags());
    }

    @Test
    void shouldUpdatePost() {
        Post post = Post.builder()
                .id(11L)
                .title("Title")
                .text("text")
                .image(Image.builder()
                        .data("IMAGE".getBytes(StandardCharsets.UTF_8))
                        .build())
                .tags(List.of(
                        Tag.builder()
                                .name("t1")
                                .build(),
                        Tag.builder()
                                .name("t2")
                                .build()
                ))
                .build();

        List<Tag> mergedTags = post.getTags().stream()
                .map(t -> t.toBuilder()
                        .id(1L)
                        .build()
                ).toList();

        when(tagRepositoryMock.merge(anyList()))
                .thenReturn(mergedTags);

        when(postRepositoryMock.findById(post.getId()))
                .thenReturn(Optional.of(post));

        postsService.update(post);

        verify(postRepositoryMock, times(1))
                .update(post);

        verify(tagRepositoryMock, times(1))
                .unboundTagsFromPost(post.getId());

        verify(tagRepositoryMock, times(1))
                .merge(post.getTags());

        verify(tagRepositoryMock, times(1))
                .bindTagsToPost(post.getId(), mergedTags);
    }

    @Test
    void shouldNotUpdateNotPresentPost() {
        when(postRepositoryMock.findById(22L))
                .thenReturn(Optional.empty());

        assertThrows(
                PostNotFoundException.class,
                () -> postsService.update(Post.builder()
                        .id(22L)
                        .build())
        );
    }

    @Test
    void shouldFindPost() {
        Post post = Post.builder()
                .id(4L)
                .title("ttt")
                .text("ssss")
                .likes(10)
                .build();

        List<Tag> tags = List.of(
                Tag.builder()
                        .id(7L)
                        .name("T1")
                        .build(),
                Tag.builder()
                        .id(8L)
                        .name("T2")
                        .build()
        );

        List<Commentary> commentaries = List.of(
                Commentary.builder()
                        .id(1L)
                        .text("C1")
                        .postId(post.getId())
                        .build(),
                Commentary.builder()
                        .id(2L)
                        .text("C2")
                        .postId(post.getId())
                        .build()
        );

        when(postRepositoryMock.findById(post.getId()))
                .thenReturn(Optional.of(post));

        when(tagRepositoryMock.findByPostId(post.getId()))
                .thenReturn(tags);

        when(commentaryRepositoryMock.findByPostIdOrderById(post.getId()))
                .thenReturn(commentaries);

        Post dbPost = postsService.findPost(post.getId());

        assertAll(
                () -> assertEquals(post.getId(), dbPost.getId()),
                () -> assertEquals(post.getTitle(), dbPost.getTitle()),
                () -> assertEquals(post.getText(), dbPost.getText()),
                () -> assertEquals(2, dbPost.getTags().size()),
                () -> assertEquals(2, dbPost.getCommentaries().size())
        );

        assertEquals(tags, dbPost.getTags());
        assertEquals(commentaries, dbPost.getCommentaries());
    }

    @Test
    void shouldThrowExceptionIfNotFoundPost() {
        when(postRepositoryMock.findById(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(
                PostNotFoundException.class,
                () -> postsService.findPost(11L)
        );
    }

    @Test
    void shouldFindPage() {
        PostsPage page = PostsPage.builder()
                .page(1)
                .totalPages(5)
                .postsPerPage(3)
                .build();

        when(postRepositoryMock.findAllPaged(any()))
                .thenReturn(page);

        PostsPage dbPage = postsService.getPostsPage(PageRequest.builder()
                .page(1)
                .posts(3)
                .build()
        );

        assertEquals(page, dbPage);
    }

    @Test
    void shouldFindImageByPostId() {
        when(imageRepositoryMock.findByPostId(5L))
                .thenReturn(Optional.of(Image.builder()
                        .data("DATA".getBytes(StandardCharsets.UTF_8))
                        .build())
                );

        byte[] data = assertDoesNotThrow(() -> postsService.getImage(5L));
        assertArrayEquals("DATA".getBytes(StandardCharsets.UTF_8), data);
    }

    @Test
    void shouldNotFindImage() {
        when(imageRepositoryMock.findByPostId(anyLong()))
                .thenReturn(Optional.empty());

        assertThrows(
                PostNotFoundException.class,
                () -> postsService.getImage(2L)
        );
    }

    @Test
    void shouldIncrementLikes() {
        postsService.incrementLikes(98L);

        verify(postRepositoryMock, times(1))
                .incrementLikes(98L);
    }

    @Test
    void shouldDeletePost() {
        postsService.delete(76L);

        verify(postRepositoryMock, times(1))
                .deleteById(76L);
    }
}