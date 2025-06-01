package ru.girqa.myblog.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.girqa.myblog.exception.PostNotFoundException;
import ru.girqa.myblog.model.domain.PageRequest;
import ru.girqa.myblog.model.domain.Tag;
import ru.girqa.myblog.model.domain.post.Image;
import ru.girqa.myblog.model.domain.post.Post;
import ru.girqa.myblog.model.domain.post.PostsPage;
import ru.girqa.myblog.repository.CommentaryRepository;
import ru.girqa.myblog.repository.ImageRepository;
import ru.girqa.myblog.repository.PostRepository;
import ru.girqa.myblog.repository.TagRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostsService {

    private final PostRepository postRepository;

    private final ImageRepository imageRepository;

    private final TagRepository tagRepository;

    private final CommentaryRepository commentaryRepository;

    @Transactional
    public Post create(@NonNull Post post) {
        post = postRepository.save(post);

        Image image = post.getImage().toBuilder()
                .postId(post.getId())
                .build();
        imageRepository.save(image);

        List<Tag> mergedTags = tagRepository.merge(post.getTags());
        tagRepository.bindTagsToPost(post.getId(), mergedTags);

        return post.toBuilder()
                .image(image)
                .tags(mergedTags)
                .build();
    }

    @Transactional
    public void update(@NonNull Post updatedPost) {
        Post dbPost = postRepository.findById(updatedPost.getId())
                .orElseThrow(PostNotFoundException::new);

        updatedPost.getImage().setPostId(dbPost.getId());
        dbPost.update(updatedPost);
        imageRepository.update(dbPost.getImage());

        postRepository.update(dbPost);
        tagRepository.unboundTagsFromPost(dbPost.getId());

        List<Tag> mergedTags = tagRepository.merge(dbPost.getTags());
        tagRepository.bindTagsToPost(dbPost.getId(), mergedTags);

    }

    @Transactional(readOnly = true)
    public Post findPost(@NonNull Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(PostNotFoundException::new);
        post.setTags(tagRepository.findByPostId(post.getId()));
        post.setCommentaries(commentaryRepository.findByPostIdOrderById(post.getId()));
        return post;
    }

    @Transactional(readOnly = true)
    public PostsPage getPostsPage(@NonNull PageRequest request) {
        return postRepository.findAllPaged(request);
    }

    @Transactional(readOnly = true)
    public byte[] getImage(@NonNull Long id) {
        return imageRepository.findByPostId(id)
                .orElseThrow(PostNotFoundException::new)
                .getData();
    }

    @Transactional
    public Integer incrementLikes(@NonNull Long id) {
        return postRepository.incrementLikes(id);
    }

    public void delete(@NonNull Long id) {
        postRepository.deleteById(id);
    }
}
