package ru.girqa.myblog.model.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PostsPageDto(
        @NotNull Integer page,
        @NotNull Integer totalPages,
        @NotNull Integer postsPerPage,
        @NotNull String targetTag,
        @NotNull List<PostPreviewDto> posts
) {
}
