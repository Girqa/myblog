package ru.girqa.myblog.controller.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ru.girqa.myblog.config.WebConfiguration;
import ru.girqa.myblog.model.mapper.CommentaryMapper;
import ru.girqa.myblog.model.mapper.PostMapper;
import ru.girqa.myblog.service.CommentaryService;
import ru.girqa.myblog.service.PostsService;

import static org.mockito.Mockito.mock;

@EnableWebMvc
@Configuration
@Import(WebConfiguration.class)
@ComponentScan(basePackages = "ru.girqa.myblog.controller")
public class TestWebConfiguration {

    @Bean
    CommentaryService commentaryService() {
        return mock(CommentaryService.class);
    }

    @Bean
    CommentaryMapper commentaryMapper() {
        return mock(CommentaryMapper.class);
    }

    @Bean
    PostsService postsService() {
        return mock(PostsService.class);
    }

    @Bean
    PostMapper postMapper() {
        return mock(PostMapper.class);
    }
}
