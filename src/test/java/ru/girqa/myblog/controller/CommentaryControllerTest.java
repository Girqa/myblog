package ru.girqa.myblog.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.MultiValueMap;
import org.testcontainers.shaded.com.google.common.net.MediaType;
import ru.girqa.myblog.model.domain.Commentary;
import ru.girqa.myblog.model.dto.commentary.CreateCommentaryDto;
import ru.girqa.myblog.model.mapper.CommentaryMapper;
import ru.girqa.myblog.service.CommentaryService;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentaryController.class)
class CommentaryControllerTest {

    @MockitoBean
    CommentaryService commentaryServiceMock;

    @MockitoBean
    CommentaryMapper commentaryMapperMock;

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void setUpMocks() {
        reset(commentaryServiceMock, commentaryMapperMock);
    }

    @Test
    @SneakyThrows
    void shouldCreateCommentary() {
        final Long POST_ID = 17L;

        when(commentaryMapperMock.toDomain(new CreateCommentaryDto(POST_ID, "SomeText")))
                .thenReturn(Commentary.builder()
                        .postId(POST_ID)
                        .text("SomeText")
                        .build()
                );

        mockMvc.perform(post("/commentaries/commentary")
                        .params(MultiValueMap.fromSingleValue(Map.of(
                                "postId", POST_ID.toString(),
                                "text", "SomeText"))
                        )
                        .contentType(MediaType.FORM_DATA.type())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/posts/post/%d".formatted(POST_ID))
                );

        verify(commentaryServiceMock, times(1))
                .save(Commentary.builder()
                        .postId(POST_ID)
                        .text("SomeText")
                        .build());
    }

    @Test
    @SneakyThrows
    void shouldUpdateCommentary() {
        final Long POST_ID = 67L;
        final String TEXT = "La tex";
        final Long COMMENT_ID = 2L;

        mockMvc.perform(put("/commentaries/commentary/{id}", COMMENT_ID)
                        .param("postId", POST_ID.toString())
                        .param("text", TEXT)
                        .contentType(MediaType.FORM_DATA.type())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/posts/post/%d".formatted(POST_ID))
                );

        verify(commentaryServiceMock, times(1))
                .update(COMMENT_ID, TEXT);
    }

    @Test
    @SneakyThrows
    void shouldDeleteCommentary() {
        final Long POST_ID = 33L;
        final Long COMMENT_ID = 91L;

        mockMvc.perform(delete("/commentaries/commentary/{id}", COMMENT_ID)
                        .contentType(MediaType.FORM_DATA.type())
                        .param("postId", POST_ID.toString())
                )
                .andExpectAll(
                        status().is3xxRedirection(),
                        redirectedUrl("/posts/post/%d".formatted(POST_ID))
                );

        verify(commentaryServiceMock, times(1))
                .delete(COMMENT_ID);
    }
}