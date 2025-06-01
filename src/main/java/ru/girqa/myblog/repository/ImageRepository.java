package ru.girqa.myblog.repository;

import lombok.NonNull;
import ru.girqa.myblog.model.domain.post.Image;

import java.util.Optional;

public interface ImageRepository {

    void save(@NonNull Image image);

    void update(@NonNull Image image);

    Optional<Image> findByPostId(@NonNull Long postId);
}
