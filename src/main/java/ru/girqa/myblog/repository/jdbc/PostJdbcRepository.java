package ru.girqa.myblog.repository.jdbc;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.girqa.myblog.model.domain.PageRequest;
import ru.girqa.myblog.model.domain.Tag;
import ru.girqa.myblog.model.domain.post.Post;
import ru.girqa.myblog.model.domain.post.PostPreview;
import ru.girqa.myblog.model.domain.post.PostsPage;
import ru.girqa.myblog.repository.PostRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostJdbcRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Post save(@NonNull Post post) {
        Long postId = jdbcTemplate.queryForObject(
                """
                        insert into posts(title, likes, post_text) values (?, ?, ?)
                        returning id
                        """,
                Long.class,
                post.getTitle(), post.getLikes(), post.getText()
        );

        if (postId == null) {
            log.error("Can not merge post {}. Database returned null id.", post);
            throw new IllegalStateException("Bad returned post id on creation");
        }

        return post.toBuilder()
                .id(postId)
                .build();
    }

    @Override
    public void update(@NonNull Post post) {
        jdbcTemplate.update(
                """
                update posts
                set title = ?, post_text = ?
                where id = ?
                """,
                post.getTitle(), post.getText(), post.getId()
        );
    }

    @Override
    public Optional<Post> findById(@NonNull Long id) {
        Optional<Post> opPost = jdbcTemplate.query(
                        """
                                select p.id, p.title, p.post_text, p.likes from posts p
                                where p.id = ?
                                """,
                        (rs, rowNum) -> extractPost(rs),
                        id
                ).stream()
                .findFirst();

        if (opPost.isEmpty()) return opPost;

        Post post = opPost.get();
        return Optional.of(post);
    }

    @Override
    public PostsPage findAllPaged(@NonNull PageRequest page) {
        Optional<Integer> totalPages;
        List<PostPreview> posts;
        if (Objects.isNull(page.getTargetTag()) || page.getTargetTag().isBlank()) {
            totalPages = jdbcTemplate.query(
                            "select ceil(count(*) / ?::float) total_pages from posts",
                            (rs, rowNum) -> rs.getInt("total_pages"),
                            page.getPosts())
                    .stream().findFirst();
            posts = jdbcTemplate.query(
                    """
                            select id, title, likes, post_text from posts
                            limit ?
                            offset ?
                            """,
                    (rs, rowNum) -> extractPreview(rs),
                    page.getPosts(), (page.getPage() - 1) * page.getPosts()
            );
        } else {
            totalPages = jdbcTemplate.query(
                            """
                                    select ceil(count(*) / ?::float) total_pages from posts p
                                    left join post_tags pt on pt.post_id = p.id
                                    left join tags t on t.id = pt.tag_id
                                    where t.tag_name = ?
                                    """,
                            (rs, rowNum) -> rs.getInt("total_pages"),
                            page.getPosts(),
                            page.getTargetTag())
                    .stream().findFirst();
            posts = jdbcTemplate.query(
                    """
                            select p.id, p.title, p.likes, p.post_text from posts p
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
        }

        fillCommentsCounts(posts);
        fillTags(posts);

        return PostsPage.builder()
                .page(page.getPage())
                .postsPerPage(page.getPosts())
                .targetTag(page.getTargetTag())
                .posts(posts)
                .totalPages(totalPages.orElse(0))
                .build();
    }

    @Override
    public Integer incrementLikes(@NonNull Long id) {
        jdbcTemplate.update("""
                update posts
                set likes = likes + 1
                where id = ?
                """, id);
        return jdbcTemplate.queryForObject("""
                select likes from posts
                where id = ?
                """, Integer.class, id);
    }

    @Override
    public void deleteById(@NonNull Long id) {
        jdbcTemplate.update("""
                delete from posts
                where id = ?
                """, id);
    }

    private void fillCommentsCounts(List<PostPreview> posts) {
        if (posts.isEmpty()) return;

        record PostComments(Long postId, Long comments) {
        }

        String inSql = String.join(", ", Collections.nCopies(posts.size(), "?"));
        String query = """
                select p.id, count(*) as comments from posts p
                left join commentaries c on p.id = c.post_id
                where p.id in (%s) and c.id is not null
                group by p.id
                """.formatted(inSql);
        Map<Long, Long> comments = jdbcTemplate.query(
                        query,
                        (rs, rowNum) -> new PostComments(
                                rs.getLong("id"),
                                rs.getLong("comments")
                        ),
                        posts.stream()
                                .map(PostPreview::getId)
                                .toArray()
                ).stream()
                .collect(Collectors.toMap(
                        PostComments::postId,
                        PostComments::comments
                ));


        for (PostPreview post : posts) {
            Long postComments = comments.getOrDefault(post.getId(), 0L);
            post.setComments(postComments);
        }
    }

    private void fillTags(List<PostPreview> posts) {
        if (posts.isEmpty()) return;

        record PostTag(Long postId, Tag tag) {
        }

        String inSql = String.join(", ", Collections.nCopies(posts.size(), "?"));
        String query = """
                select p.id post_id, t.tag_name tag_name, t.id tag_id from posts p
                left join post_tags pt on pt.post_id = p.id
                left join tags t on t.id = pt.tag_id
                where p.id in (%s)
                """.formatted(inSql);
        Map<Long, List<Tag>> tags = jdbcTemplate.query(
                        query,
                        (rs, rowNum) -> new PostTag(
                                rs.getLong("post_id"),
                                Tag.builder()
                                        .id(rs.getLong("tag_id"))
                                        .name(rs.getString("tag_name"))
                                        .build()
                        ),
                        posts.stream()
                                .map(PostPreview::getId)
                                .toArray()
                ).stream()
                .collect(Collectors.groupingBy(
                        PostTag::postId,
                        Collectors.mapping(PostTag::tag, Collectors.toList())
                ));


        for (PostPreview post : posts) {
            List<Tag> postTags = tags.getOrDefault(post.getId(), new ArrayList<>());
            post.setTags(postTags);
        }
    }

    private static Post extractPost(ResultSet rs) throws SQLException {
        return Post.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .text(rs.getString("post_text"))
                .likes(rs.getInt("likes"))
                .build();
    }

    protected PostPreview extractPreview(ResultSet rs) throws SQLException {
        return PostPreview.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .likes(rs.getInt("likes"))
                .text(rs.getString("post_text"))
                .build();
    }
}
