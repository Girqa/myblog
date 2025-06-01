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
import ru.girqa.myblog.model.domain.post.Post;
import ru.girqa.myblog.model.domain.post.PostsPage;
import ru.girqa.myblog.repository.CommentaryRepository;
import ru.girqa.myblog.repository.ImageRepository;
import ru.girqa.myblog.repository.PostRepository;
import ru.girqa.myblog.repository.TagRepository;

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
        Post post = Post.builder()
                .title("Title")
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
                        .id(5L)
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

        Post saved = postsService.create(post);  // TODO: add check for image

        verify(postRepositoryMock, times(1))
                .save(post);

        verify(tagRepositoryMock, times(1))
                .merge(post.getTags());

        verify(tagRepositoryMock, times(1))
                .bindTagsToPost(saved.getId(), mergedTags);

        assertAll(
                () -> assertEquals(5L, saved.getId()),
                () -> assertEquals(post.getTitle(), saved.getTitle()),
                () -> assertEquals(post.getText(), saved.getText()),
                () -> assertEquals(2, saved.getTags().size())
        );

        assertEquals(mergedTags, saved.getTags());
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
}