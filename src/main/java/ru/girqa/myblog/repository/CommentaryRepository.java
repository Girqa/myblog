package ru.girqa.myblog.repository;

import lombok.NonNull;
import ru.girqa.myblog.model.domain.Commentary;

import java.util.List;
import java.util.Optional;

public interface CommentaryRepository {

    Commentary save(@NonNull Commentary commentary);

    Optional<Commentary> findById(@NonNull Long id);

    void update(@NonNull Commentary commentary);

    void delete(@NonNull Long id);

    List<Commentary> findByPostId(@NonNull Long postId);

    Long countByPostId(@NonNull Long postId);
}
