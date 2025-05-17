package ru.girqa.myblog.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class Tag {
    private Long id;
    private String name;

    public Tag(String name) {
        this.name = name;
    }

}
