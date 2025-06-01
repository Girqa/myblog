package ru.girqa.myblog.model.dto.commentary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentaryDto {
    @NotNull
    private Long postId;
    @NotBlank
    private String text;
}
