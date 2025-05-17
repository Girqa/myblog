package ru.girqa.myblog.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.girqa.myblog.model.domain.post.Post;
import ru.girqa.myblog.model.domain.post.PostPreview;
import ru.girqa.myblog.model.dto.CreatePostDto;
import ru.girqa.myblog.model.dto.PostDto;
import ru.girqa.myblog.model.dto.PostPreviewDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        uses = {TagMapper.class, CommentaryMapper.class})
public interface PostMapper {

    @Mapping(target = "likes", constant = "0")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "commentaries", ignore = true)
    Post toDomain(CreatePostDto dto);

    PostDto toDto(Post domain);

    @Mapping(target = "tags", source = "tags")
    PostPreviewDto toDto(PostPreview domain);

}
