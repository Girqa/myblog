package ru.girqa.myblog.model.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import ru.girqa.myblog.model.dto.commentary.CommentaryDto;

import java.util.List;

@Data
@Builder
public class PostDto {
    @NotNull
    private long id;
    @NotBlank
    private String title;
    @NotNull
    private long likes;
    @NotNull
    private String text;
    @NotNull
    private List<String> tags;
    @NotNull
    private List<CommentaryDto> commentaries;
}
