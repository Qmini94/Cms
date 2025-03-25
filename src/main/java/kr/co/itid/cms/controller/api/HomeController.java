package kr.co.itid.cms.controller.api;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/login")
    public String login() {
        return "/login"; // login.html 직접 반환
    }

    @GetMapping("/www/index")
    public String index(Authentication authentication, Model model) {
        if (authentication != null) {
            String role = authentication.getAuthorities().toString();
            Object data = authentication.getPrincipal();

            model.addAttribute("role", role);
            model.addAttribute("data", data);
            model.addAttribute("content", "www/main/main");

            return "/www/index";
        }
        return "redirect:/login";
    }

    @GetMapping("/denied")
    public String accessDenied() {
        return "/access-denied"; // 권한 거부 페이지
    }
}
