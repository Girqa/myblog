package ru.girqa.myblog.model.domain.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import ru.girqa.myblog.model.domain.Commentary;
import ru.girqa.myblog.model.domain.Tag;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class Post {
    private Long id;
    private String title;
    private Integer likes;
    private String text;
    private Image image;
    private List<Tag> tags;
    private List<Commentary> commentaries = new ArrayList<>();

    public Post(
            @NonNull String title,
            @NonNull Image image,
            @NonNull String text,
            @NonNull List<Tag> tags
    ) {
        this.title = title;
        this.image = image;
        this.text = text;
        this.tags = tags;
    }

    public void update(@NonNull Post updatedPost) {
        this.title = updatedPost.getTitle();
        this.text = updatedPost.getText();
        this.tags = updatedPost.getTags();
        this.image = updatedPost.getImage();
    }
}
