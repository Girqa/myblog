package ru.girqa.myblog.repository;

import lombok.NonNull;
import ru.girqa.myblog.model.domain.PageRequest;
import ru.girqa.myblog.model.domain.post.Post;
import ru.girqa.myblog.model.domain.post.PostsPage;

import java.util.Optional;

public interface PostRepository {
    Post save(@NonNull Post post);

    void update(@NonNull Post post);

    Optional<Post> findById(@NonNull Long id);

    PostsPage findAllPaged(@NonNull PageRequest page);

    Integer incrementLikes(@NonNull Long id);

    void deleteById(@NonNull Long id);
}
