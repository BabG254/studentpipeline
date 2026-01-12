package com.studentpipeline.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "datapath")
public class DataPathConfig {
    
    private String base = "C:/var/log/applications/API/dataprocessing";
    
    public String getBase() {
        return base;
    }
    
    public void setBase(String base) {
        this.base = base;
    }
}