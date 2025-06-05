package ru.girqa.myblog.model.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostDto {
    @NotBlank
    private String title;
    @NotNull
    private MultipartFile image;
    @NotBlank
    private String text;
    @NotNull
    private List<String> tags = new ArrayList<>();
}
