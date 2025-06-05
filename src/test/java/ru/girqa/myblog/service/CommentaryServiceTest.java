package ru.girqa.myblog.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.girqa.myblog.model.domain.Commentary;
import ru.girqa.myblog.repository.CommentaryRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentaryServiceTest {

    @Mock
    CommentaryRepository repositoryMock;

    @InjectMocks
    CommentaryService commentaryService;

    @BeforeEach
    void setUpMocks() {
        reset(repositoryMock);
    }

    @Test
    void shouldSave() {
        Commentary commentary = Commentary.builder()
                .postId(2L)
                .text("Comment")
                .build();

        when(repositoryMock.save(any()))
                .thenReturn(commentary.toBuilder()
                        .id(3L)
                        .build()
                );

        Commentary saved = commentaryService.save(commentary);

        assertAll(
                () -> assertEquals(3L, saved.getId()),
                () -> assertEquals(2L, saved.getPostId()),
                () -> assertEquals("Comment", saved.getText())
        );

        verify(repositoryMock, times(1))
                .save(commentary);
    }

    @Test
    void shouldUpdateCommentary() {
        Commentary dbComment = Commentary.builder()
                .id(3L)
                .postId(2L)
                .text("Comment")
                .build();

        Commentary updatedComment = dbComment.toBuilder()
                .text("New comment text")
                .build();

        when(repositoryMock.findById(dbComment.getId()))
                .thenReturn(Optional.of(dbComment));

        doNothing()
                .when(repositoryMock)
                .update(any());

        commentaryService.update(dbComment.getId(), updatedComment.getText());

        verify(repositoryMock, times(1))
                .update(updatedComment);
    }

}