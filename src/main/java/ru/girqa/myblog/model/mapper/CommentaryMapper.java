package ru.girqa.myblog.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.girqa.myblog.model.domain.Commentary;
import ru.girqa.myblog.model.dto.commentary.CreateCommentaryDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CommentaryMapper {

    @Mapping(target = "id", ignore = true)
    Commentary toDomain(CreateCommentaryDto dto);
}
