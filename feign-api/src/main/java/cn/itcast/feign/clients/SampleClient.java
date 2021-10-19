package cn.itcast.feign.clients;

import cn.itcast.feign.config.DefaultFeignClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "sampleservice", configuration = DefaultFeignClientConfiguration.class)
public interface SampleClient {
}
