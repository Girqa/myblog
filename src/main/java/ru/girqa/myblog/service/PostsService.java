package ru.girqa.myblog.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.girqa.myblog.exception.PostNotFoundException;
import ru.girqa.myblog.model.domain.PageRequest;
import ru.girqa.myblog.model.domain.Tag;
import ru.girqa.myblog.model.domain.post.Post;
import ru.girqa.myblog.model.domain.post.PostsPage;
import ru.girqa.myblog.repository.CommentaryRepository;
import ru.girqa.myblog.repository.PostRepository;
import ru.girqa.myblog.repository.TagRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostsService {

    private final PostRepository postRepository;

    private final TagRepository tagRepository;

    private final CommentaryRepository commentaryRepository;

    @Transactional
    public Post create(@NonNull Post post) {
        post = postRepository.save(post);
        List<Tag> mergedTags = tagRepository.merge(post.getTags());
        tagRepository.bindTagsToPost(post.getId(), mergedTags);
        return post.toBuilder()
                .tags(mergedTags)
                .build();
    }

    @Transactional(readOnly = true)
    public Post findPost(@NonNull Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(PostNotFoundException::new);
        post.setTags(tagRepository.findByPostId(post.getId()));
        post.setCommentaries(commentaryRepository.findByPostId(post.getId()));
        return post;
    }

    @Transactional(readOnly = true)
    public PostsPage getPostsPage(@NonNull PageRequest request) {
        return postRepository.findAllPaged(request);
    }
}
