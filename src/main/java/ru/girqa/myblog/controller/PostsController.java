package ru.girqa.myblog.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.girqa.myblog.model.domain.PageRequest;
import ru.girqa.myblog.model.domain.post.Post;
import ru.girqa.myblog.model.domain.post.PostsPage;
import ru.girqa.myblog.model.dto.commentary.CreateCommentaryDto;
import ru.girqa.myblog.model.dto.post.CreatePostDto;
import ru.girqa.myblog.model.dto.post.UpdatePostDto;
import ru.girqa.myblog.model.mapper.PostMapper;
import ru.girqa.myblog.service.PostsService;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostsController {

    public final static int DEFAULT_PAGE = 1;

    public final static int DEFAULT_POSTS_PER_PAGE = 10;

    public final static List<Integer> POST_PER_PAGE_OPTIONS = List.of(10, 20, 50);

    private final PostsService postsService;

    private final PostMapper postMapper;

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String getAllPosts(Model model,
                              @RequestParam(required = false, name = "page", defaultValue = "1") Integer page,
                              @RequestParam(required = false, name = "postsPerPage", defaultValue = "10") Integer postsPerPage,
                              @RequestParam(required = false, name = "tag") String tag) {
        PostsPage postsPage = postsService.getPostsPage(PageRequest.builder()
                .page(page == null ? DEFAULT_PAGE : page)
                .posts(postsPerPage == null ? DEFAULT_POSTS_PER_PAGE : postsPerPage)
                .targetTag(tag != null ? tag.trim() : null)
                .build());

        model.addAttribute("newPost", new CreatePostDto());
        model.addAttribute("posts", postsPage.getPosts().stream()
                .map(postMapper::toDto)
                .toList());
        model.addAttribute("page", postsPage.getPage());
        model.addAttribute("postsPerPage", postsPerPage);
        model.addAttribute("availablePostsPerPage", POST_PER_PAGE_OPTIONS);
        model.addAttribute("totalPages", postsPage.getTotalPages());
        model.addAttribute("searchTag", postsPage.getTargetTag() == null ? "" : postsPage.getTargetTag());
        return "all-posts";
    }

    @GetMapping(path = "/post/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String getPost(@PathVariable("id") Long id,
                          Model model) {
        Post post = postsService.findPost(id);
        model.addAttribute("post", postMapper.toDto(post));
        model.addAttribute("editPost", postMapper.toUpdateDto(post));
        model.addAttribute("createCommentary", new CreateCommentaryDto(post.getId(), ""));
        return "post";
    }

    @PostMapping("/post")
    public String createPost(@ModelAttribute("newPost") CreatePostDto postDto) {
        Post post = postMapper.toDomain(postDto);
        postsService.create(post);
        return "redirect:/posts";
    }

    @PostMapping("/post/{id}")
    public String updatePost(@PathVariable("id") Long id, @ModelAttribute("editPost") UpdatePostDto postDto) {
        Post post = postMapper.toDomain(postDto);
        post.setId(id);
        postsService.update(post);
        return "redirect:/posts/post/%d".formatted(id);
    }

    @DeleteMapping("/post/{id}")
    public String deletePost(@PathVariable("id") Long id) {
        postsService.delete(id);
        return "redirect:/posts";
    }

    @GetMapping(path = "/post/{id}/image", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<byte[]> getImage(@PathVariable("id") Long id) {
        return ResponseEntity.ok(postsService.getImage(id));
    }

    @PostMapping(path = "/post/{id}/like", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> incrementLikes(@PathVariable("id") Long id) {
        return ResponseEntity
                .ok(postsService.incrementLikes(id).toString());
    }
}
