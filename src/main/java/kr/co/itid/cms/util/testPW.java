package kr.co.itid.cms.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class testPW {
    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String rawPassword = "seongmin";  // 원하는 가짜 비밀번호
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("Encoded password: " + encodedPassword);
    }

}

