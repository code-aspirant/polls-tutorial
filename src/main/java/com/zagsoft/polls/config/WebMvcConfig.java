package com.zagsoft.polls.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
 * We’ll be accessing the APIs from the react client that will run on its own development server.
 * create the following WebMvcConfig class inside com.example.polls.config package to enable cross origin requests globally
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**");
    }

}
