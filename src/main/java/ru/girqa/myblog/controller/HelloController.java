package ru.girqa.myblog.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/")
public class HelloController {

    @GetMapping("hello")
    public @ResponseBody String hello() {
        return """
                <html>
                <body>
                    <h2>Hello World!</h2>
                </body>
                </html>
               """;
    }
}
