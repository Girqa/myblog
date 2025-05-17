package ru.girqa.myblog.model.domain.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import ru.girqa.myblog.model.domain.Commentary;
import ru.girqa.myblog.model.domain.Tag;

import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@EqualsAndHashCode
public class Post {
    private Long id;
    private String title;
    private Blob image;
    private Integer likes;
    private String text;
    private List<Tag> tags;
    private List<Commentary> commentaries = new ArrayList<>();

    public Post(
            @NonNull String title,
            @NonNull Blob image,
            @NonNull String text,
            @NonNull List<Tag> tags
    ) {
        this.title = title;
        this.image = image;
        this.text = text;
        this.tags = tags;
    }
}
