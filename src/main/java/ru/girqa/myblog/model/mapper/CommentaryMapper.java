package ru.girqa.myblog.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.girqa.myblog.model.domain.Commentary;
import ru.girqa.myblog.model.dto.CommentaryDto;
import ru.girqa.myblog.model.dto.CreateCommentaryDto;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentaryMapper {

    CommentaryDto toDto(Commentary domain);

    List<CommentaryDto> toDto(List<Commentary> domain);

    Commentary toDomain(CommentaryDto dto);

    @Mapping(target = "id", ignore = true)
    Commentary toDomain(CreateCommentaryDto dto);
}
