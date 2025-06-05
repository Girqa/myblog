package ru.girqa.myblog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.girqa.myblog.model.domain.Commentary;
import ru.girqa.myblog.model.dto.commentary.CreateCommentaryDto;
import ru.girqa.myblog.model.mapper.CommentaryMapper;
import ru.girqa.myblog.service.CommentaryService;

@Controller
@RequestMapping("/commentaries")
@RequiredArgsConstructor
public class CommentaryController {

    private final CommentaryService commentaryService;

    private final CommentaryMapper commentaryMapper;

    @PostMapping("/commentary")
    public String create(@ModelAttribute("createCommentary") CreateCommentaryDto newCommentary) {
        Commentary commentary = commentaryMapper.toDomain(newCommentary);
        commentaryService.save(commentary);
        return "redirect:/posts/post/%d".formatted(newCommentary.getPostId());
    }

    @PutMapping("/commentary/{id}")
    public String update(@ModelAttribute("text") String text,
                         @ModelAttribute("postId") Long postId,
                         @PathVariable("id") Long commentaryId) {
        commentaryService.update(commentaryId, text);
        return "redirect:/posts/post/%d".formatted(postId);
    }

    @DeleteMapping("/commentary/{id}")
    public String delete(@ModelAttribute("postId") Long postId,
                         @PathVariable("id") Long commentaryId) {
        commentaryService.delete(commentaryId);
        return "redirect:/posts/post/%d".formatted(postId);
    }
}
