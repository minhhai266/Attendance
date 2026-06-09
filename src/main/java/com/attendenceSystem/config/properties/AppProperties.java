/*
Đọc file config từ application.properties
*/

package com.attendenceSystem.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "app")
@Getter
@Setter
public class AppProperties {
    private String timezone;
}