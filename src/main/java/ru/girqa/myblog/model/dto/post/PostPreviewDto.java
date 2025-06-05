package ru.girqa.myblog.model.dto.post;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostPreviewDto(
        @NotNull Long id,
        @NotNull String title,
        @NotNull String text,
        @NotNull Integer likes,
        @NotNull Integer comments,
        @NotNull List<String> tags
) {
}
