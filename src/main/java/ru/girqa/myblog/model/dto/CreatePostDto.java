package ru.girqa.myblog.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.sql.Blob;
import java.util.List;

public record CreatePostDto(
        @NotBlank String title,
        @NotNull Blob image,
        @NotBlank String text,
        @NotNull List<String> tags
) {
}
