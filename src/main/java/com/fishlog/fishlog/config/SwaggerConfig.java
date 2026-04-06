package com.fishlog.fishlog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("FishLog API")
                        .description("""
                                낚시 기록 관리 서비스 API 문서

                                **주요 기능**
                                - 낚시 기록 CRUD (제목, 어종, 날짜, 위치, 날씨 등)
                                - 사진 업로드 및 AI 어종 자동 분류 (Google Gemini Vision)
                                - 개인 연간 낚시 통계 리포트
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("FishLog Team")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("로컬 개발 서버")
                ));
    }
}
