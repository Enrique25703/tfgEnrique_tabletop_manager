package org.example.tfgenrique.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/deployments40k/**")
                .addResourceLocations("classpath:/deployments40k/");
        registry.addResourceHandler("/profilepicks/**")
                .addResourceLocations("classpath:/profilepicks/");
        registry.addResourceHandler("/menupicks/**")
                .addResourceLocations("classpath:/menupicks/");
        registry.addResourceHandler("/unitTypes40k/**")
                .addResourceLocations("classpath:/unitTypes40k/");
    }
}
