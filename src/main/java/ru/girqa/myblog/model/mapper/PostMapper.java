package ru.girqa.myblog.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.springframework.web.multipart.MultipartFile;
import ru.girqa.myblog.model.domain.post.Image;
import ru.girqa.myblog.model.domain.post.Post;
import ru.girqa.myblog.model.domain.post.PostPreview;
import ru.girqa.myblog.model.dto.post.CreatePostDto;
import ru.girqa.myblog.model.dto.post.PostDto;
import ru.girqa.myblog.model.dto.post.PostPreviewDto;
import ru.girqa.myblog.model.dto.post.UpdatePostDto;

import java.io.IOException;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {TagMapper.class, CommentaryMapper.class})
public interface PostMapper {

    @Mapping(target = "likes", constant = "0")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "commentaries", ignore = true)
    Post toDomain(CreatePostDto dto);

    @Mapping(target = "likes", constant = "0")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "commentaries", ignore = true)
    Post toDomain(UpdatePostDto dto);

    PostDto toDto(Post domain);

    @Mapping(target = "tags", source = "tags")
    @Mapping(target = "text", source = "text", qualifiedByName = "shortenText")
    PostPreviewDto toDto(PostPreview domain);

    @Mapping(target = "image", ignore = true)
    @Mapping(target = "tags", ignore = true)
    UpdatePostDto toUpdateDto(Post post);

    default Image map(MultipartFile file) {
        try {
            return Image.builder()
                    .name(file.getName())
                    .size(file.getSize())
                    .data(file.getBytes())
                    .build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Named("shortenText")
    default String shortenText(String text) {
        String[] parts = text.split("\n");
        if (parts.length == 1) return parts[0];
        for (String part: parts) {
            if (!part.isBlank()) return part;
        }
        return "";
    }
}
