package ru.girqa.myblog.repository;

import lombok.NonNull;
import ru.girqa.myblog.model.domain.Tag;

import java.util.List;

public interface TagRepository {

    List<Tag> findByPostId(@NonNull Long postId);

    void bindTagsToPost(@NonNull Long postId, @NonNull List<Tag> tags);

    List<Tag> merge(@NonNull List<Tag> tags);
}
