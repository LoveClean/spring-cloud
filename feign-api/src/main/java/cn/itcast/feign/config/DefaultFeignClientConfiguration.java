package cn.itcast.feign.config;

import feign.Logger;
import org.springframework.context.annotation.Bean;

public class DefaultFeignClientConfiguration {
    @Bean
    public Logger.Level feignLogLevel() {
        return Logger.Level.BASIC;
    }
}

