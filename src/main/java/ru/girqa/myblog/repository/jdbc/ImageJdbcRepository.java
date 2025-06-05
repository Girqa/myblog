package ru.girqa.myblog.repository.jdbc;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.girqa.myblog.model.domain.post.Image;
import ru.girqa.myblog.repository.ImageRepository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ImageJdbcRepository implements ImageRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(@NonNull Image image) {
        jdbcTemplate.update("""
                        insert into images(post_id, image_name, image_size, image_data)
                        values (?, ?, ?, ?);
                        """,
                image.getPostId(),
                image.getName(),
                image.getSize(),
                image.getData()
        );
    }

    @Override
    public void update(@NonNull Image image) {
        jdbcTemplate.update("""
                        update images
                        set image_name = ?, image_size = ?, image_data = ?
                        where post_id = ?
                        """,
                image.getName(),
                image.getSize(),
                image.getData(),
                image.getPostId()
        );
    }

    @Override
    public Optional<Image> findByPostId(@NonNull Long postId) {
        return jdbcTemplate.query("""
                                select i.post_id, i.image_name, i.image_size, i.image_data from images i
                                where i.post_id = ?
                                """,
                        (rs, n) -> Image.builder()
                                .postId(rs.getLong("post_id"))
                                .name(rs.getString("image_name"))
                                .size(rs.getLong("image_size"))
                                .data(rs.getBytes("image_data"))
                                .build(),
                        postId
                ).stream()
                .findFirst();
    }
}
