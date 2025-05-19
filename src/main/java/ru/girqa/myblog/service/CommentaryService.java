package ru.girqa.myblog.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.girqa.myblog.model.domain.Commentary;
import ru.girqa.myblog.repository.CommentaryRepository;

@Service
@RequiredArgsConstructor
public class CommentaryService {

    private final CommentaryRepository repository;

    @Transactional
    public Commentary save(@NonNull Commentary commentary) {
        return repository.save(commentary);
    }

    @Transactional
    public void update(@NonNull Commentary commentary) {
        repository.update(commentary);
    }
}
