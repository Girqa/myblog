package ru.girqa.myblog.model.dto;

import jakarta.validation.constraints.NotNull;

import java.sql.Blob;
import java.util.List;

public record PostPreviewDto(
        @NotNull Long id,
        @NotNull Blob image,
        @NotNull Integer likes,
        @NotNull String text,
        @NotNull Integer comments,
        @NotNull List<String> tags
) {
}
