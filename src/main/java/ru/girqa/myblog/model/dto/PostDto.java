package ru.girqa.myblog.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.sql.Blob;
import java.util.List;

public record PostDto(
        @NotNull Long id,
        @NotBlank String title,
        @NotNull Blob image,
        @NotNull Integer likes,
        @NotNull String text,
        @NotNull List<String> tags,
        @NotNull List<CommentaryDto> commentaries
        ) {
}
