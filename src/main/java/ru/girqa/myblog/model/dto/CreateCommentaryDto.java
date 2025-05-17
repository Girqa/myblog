package ru.girqa.myblog.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCommentaryDto(
        @NotNull Long postId,
        @NotBlank String text
) {
}
