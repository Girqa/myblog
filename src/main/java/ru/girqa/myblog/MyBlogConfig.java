package ru.girqa.myblog;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import ru.girqa.myblog.config.YamlPropertySourceFactory;

@EnableWebMvc
@Configuration
@ComponentScan(basePackages = "ru.girqa.myblog")
@PropertySource(
        value = "classpath:application.yml",
        factory = YamlPropertySourceFactory.class
)
public class MyBlogConfig {
}

