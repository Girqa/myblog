package ru.girqa.myblog.model.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.girqa.myblog.model.domain.Tag;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TagMapper {

    default String toDto(Tag tag) {
        return tag.getName();
    }

    default Tag toDomain(String name) {
        return new Tag(name);
    }
}
