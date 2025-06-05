package ru.girqa.myblog.model.domain.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import ru.girqa.myblog.model.domain.Tag;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class PostPreview {
    private Long id;
    private String title;
    private Integer likes;
    private String text;
    private Long comments;
    private List<Tag> tags;
}
