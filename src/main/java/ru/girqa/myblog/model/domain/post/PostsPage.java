package ru.girqa.myblog.model.domain.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class PostsPage {
    private List<PostPreview> posts;
    private Integer page;
    private Integer totalPages;
    private Integer postsPerPage;
    private String targetTag;
}
