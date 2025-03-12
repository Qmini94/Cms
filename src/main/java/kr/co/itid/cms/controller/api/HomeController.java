package kr.co.itid.cms.controller.api;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("message", "Spring Boot CMS 사이트 실행됨!");
        return "home/index";
    }
}
