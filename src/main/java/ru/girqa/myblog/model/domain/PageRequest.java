package ru.girqa.myblog.model.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;
import org.springframework.lang.Nullable;

@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class PageRequest {
    @NonNull
    private final Integer page;
    @NonNull
    private final Integer posts;
    @Nullable
    private String targetTag;
}
