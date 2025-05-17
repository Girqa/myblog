package ru.girqa.myblog.repository.jdbc;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.girqa.myblog.model.domain.Commentary;
import ru.girqa.myblog.model.domain.PageRequest;
import ru.girqa.myblog.model.domain.Tag;
import ru.girqa.myblog.model.domain.post.Post;
import ru.girqa.myblog.model.domain.post.PostPreview;
import ru.girqa.myblog.model.domain.post.PostsPage;
import ru.girqa.myblog.repository.CommentaryRepository;
import ru.girqa.myblog.repository.PostRepository;
import ru.girqa.myblog.repository.TagRepository;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostJdbcRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    private final CommentaryRepository commentaryRepository;

    private final TagRepository tagRepository;

    @Override
    @Transactional
    public Post save(@NonNull Post post) {
        byte[] image;
        try {
            image = post.getImage().getBinaryStream().readAllBytes();
        } catch (IOException | SQLException e) {
            throw new IllegalArgumentException("Bad image given");
        }
        Long postId = jdbcTemplate.queryForObject(
                """
                        insert into posts(title, image, likes, post_text) values (?, ?, ?, ?)
                        returning id
                        """,
                Long.class,
                post.getTitle(), image, post.getLikes(), post.getText()
        );

        if (postId == null) {
            log.error("Can not merge post {}. Database returned null id.", post);
            throw new IllegalStateException("Bad returned post id on creation");
        }

        List<Tag> tags = tagRepository.merge(post.getTags());
        tagRepository.bindTagsToPost(postId, tags);

        return post.toBuilder()
                .id(postId)
                .tags(tags)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Post> findById(@NonNull Long id) {
        Optional<Post> opPost = jdbcTemplate.query(
                        """
                                select p.id, p.title, p.image, p.post_text, p.likes from posts p
                                where p.id = ?
                                """,
                        (rs, rowNum) -> extractPost(rs),
                        id
                ).stream()
                .findFirst();

        if (opPost.isEmpty()) return opPost;

        Post post = opPost.get();
        post = fillPostDependencies(post);

        return Optional.of(post);
    }

    @Override
    @Transactional(readOnly = true)
    public PostsPage findAllPaged(@NonNull PageRequest page) {
        Optional<Integer> totalPages;
        List<PostPreview> posts;
        if (Objects.isNull(page.getTargetTag())) {
            totalPages = jdbcTemplate.query(
                            "select count(*) / ? total_pages from posts",
                            (rs, rowNum) -> rs.getInt("total_pages"),
                            page.getPosts())
                    .stream().findFirst();
            posts = jdbcTemplate.query(
                    """
                            select id, title, image, likes, post_text from posts
                            limit ?
                            offset ?
                            """,
                    (rs, rowNum) -> extractPreview(rs),
                    page.getPosts(), (page.getPage() - 1) * page.getPosts()
            );
        } else {
            posts = jdbcTemplate.query(
                    """
                            select p.id, p.title, p.image, p.likes, p.post_text from posts p
                            left join post_tags pt on pt.post_id = p.id
                            left join tags t on t.id = pt.tag_id
                            where t.tag_name = ?
                            group by p.id
                            limit ?
                            offset ?
                            """,
                    (rs, rowNum) -> extractPreview(rs),
                    page.getTargetTag(), page.getPosts(), (page.getPage() - 1) * page.getPosts()
            );
            totalPages = jdbcTemplate.query(
                            """
                                    select count(*) / ? total_pages from posts p
                                    left join post_tags pt on pt.post_id = p.id
                                    left join tags t on t.id = pt.tag_id
                                    where t.tag_name = ?
                                    group by p.id
                                    """,
                            (rs, rowNum) -> rs.getInt("total_pages"),
                            page.getPosts(),
                            page.getTargetTag())
                    .stream().findFirst();
        }

        for (PostPreview post : posts) {
            post.setTags(tagRepository.findByPostId(post.getId()));
            post.setComments(commentaryRepository.countByPostId(post.getId()));
        }

        return PostsPage.builder()
                .page(page.getPage())
                .postsPerPage(page.getPosts())
                .targetTag(page.getTargetTag())
                .posts(posts)
                .totalPages(totalPages.orElse(0))
                .build();
    }

    private static Post extractPost(ResultSet rs) throws SQLException {
        return Post.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .image(new SerialBlob(rs.getBytes("image")))
                .text(rs.getString("post_text"))
                .likes(rs.getInt("likes"))
                .build();
    }

    protected Post fillPostDependencies(Post post) {
        List<Tag> tags = tagRepository.findByPostId(post.getId());
        List<Commentary> commentaries = commentaryRepository.findByPostId(post.getId());

        post = post.toBuilder()
                .tags(tags)
                .commentaries(commentaries)
                .build();
        return post;
    }

    protected PostPreview extractPreview(ResultSet rs) throws SQLException {
        return PostPreview.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .image(new SerialBlob(rs.getBytes("image")))
                .likes(rs.getInt("likes"))
                .text(rs.getString("post_text"))
                .build();
    }
}
