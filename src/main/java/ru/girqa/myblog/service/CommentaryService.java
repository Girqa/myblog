package ru.girqa.myblog.service;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.girqa.myblog.exception.CommentaryNotFoundException;
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
    public void update(@NonNull Long id, @NonNull String text) {
        Commentary commentary = repository.findById(id)
                .orElseThrow(CommentaryNotFoundException::new);
        commentary.setText(text);
        repository.update(commentary);
    }

    @Transactional
    public void delete(@NonNull Long commentaryId) {
        repository.delete(commentaryId);
    }
}
