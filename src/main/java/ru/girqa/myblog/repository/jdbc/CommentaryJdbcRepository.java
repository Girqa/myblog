package ru.girqa.myblog.repository.jdbc;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.girqa.myblog.model.domain.Commentary;
import ru.girqa.myblog.repository.CommentaryRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CommentaryJdbcRepository implements CommentaryRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Commentary save(@NonNull Commentary commentary) {
        Long id = jdbcTemplate.queryForObject("""
                        insert into commentaries(post_id, commentary_text)
                        values (?, ?)
                        returning id
                        """,
                Long.class,
                commentary.getPostId(), commentary.getText()
        );
        return commentary.toBuilder()
                .id(id)
                .build();
    }

    @Override
    public Optional<Commentary> findById(@NonNull Long id) {
        return jdbcTemplate.query("""
                select id, post_id, commentary_text from commentaries
                where id = ?
                """,
                (rs, rowNum) -> extractCommentary(rs),
                id
                ).stream().findFirst();
    }

    @Override
    public void update(@NonNull Commentary commentary) {
        jdbcTemplate.update("""
                            update commentaries
                            set commentary_text = ?
                            where id = ?
                        """,
                commentary.getText(), commentary.getId());
    }

    @Override
    public void delete(@NonNull Long id) {
        jdbcTemplate.update("""
                                    delete from commentaries
                                    where id = ?
                """, id);
    }

    @Override
    public List<Commentary> findByPostId(@NonNull Long postId) {
        return jdbcTemplate.query("""
                        select id, post_id, commentary_text from commentaries
                        where post_id = ?
                        """,
                (rs, rowNum) -> extractCommentary(rs),
                postId
        );
    }

    @Override
    public Long countByPostId(@NonNull Long postId) {
        return jdbcTemplate.queryForObject("""
                        select count(*) from commentaries
                        where post_id = ?
                        """,
                Long.class,
                postId);
    }

    private static Commentary extractCommentary(ResultSet rs) throws SQLException {
        return Commentary.builder()
                .id(rs.getLong("id"))
                .postId(rs.getLong("post_id"))
                .text(rs.getString("commentary_text"))
                .build();
    }
}
