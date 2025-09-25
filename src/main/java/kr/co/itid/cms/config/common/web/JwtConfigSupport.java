package kr.co.itid.cms.config.common.web;

import kr.co.itid.cms.config.security.model.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfigSupport {}
