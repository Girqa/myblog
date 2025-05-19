package ru.girqa.myblog.repository.jdbc;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.girqa.myblog.model.domain.Tag;
import ru.girqa.myblog.repository.TagRepository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class TagJdbcRepository implements TagRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Tag> findByPostId(@NonNull Long postId) {
        return jdbcTemplate.query(
                """
                        select t.id, t.tag_name from tags t
                        left join post_tags pt on pt.tag_id = t.id
                        where pt.post_id = ?
                        """,
                (rs, rowNum) -> extractTag(rs),
                postId
        );
    }

    @Override
    public void bindTagsToPost(@NonNull Long postId, @NonNull List<Tag> tags) {

        jdbcTemplate.batchUpdate(
                """
                        insert into post_tags (post_id, tag_id)
                        values (?, ?)
                        """,
                tags,
                tags.size(),
                (ps, tag) -> {
                    ps.setLong(1, postId);
                    ps.setLong(2, tag.getId());
                }
        );
    }

    @Override
    public List<Tag> merge(@NonNull List<Tag> tags) {
        if (tags.isEmpty()) return new ArrayList<>();

        jdbcTemplate.batchUpdate(
                """
                        insert into tags(tag_name)
                        values (?)
                        on conflict do nothing
                        """,
                tags,
                tags.size(),
                (ps, tag) -> ps.setString(1, tag.getName())
        );

        return findByNames(tags.stream()
                .map(Tag::getName)
                .toList()
        );
    }

    protected List<Tag> findByNames(List<String> names) {
        if (names.isEmpty()) return new ArrayList<>();

        String inSql = String.join(", ", Collections.nCopies(names.size(), "?"));
        String query = """
                select t.id, t.tag_name from tags t
                where t.tag_name in (%s)
                """.formatted(inSql);

        return jdbcTemplate.query(
                query,
                (rs, rowNum) -> extractTag(rs),
                names.toArray()
        );
    }

    protected Tag extractTag(ResultSet rs) throws SQLException {
        return Tag.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("tag_name"))
                .build();
    }
}
