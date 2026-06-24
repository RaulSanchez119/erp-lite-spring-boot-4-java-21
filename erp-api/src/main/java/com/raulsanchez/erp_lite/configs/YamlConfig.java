package com.raulsanchez.erp_lite.configs;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.raulsanchez.erp_lite.persistence.aws.models.AwsConfigModel;

@Configuration
@EnableConfigurationProperties(AwsConfigModel.class)
@PropertySource(value="classpath:aws/aws.yaml", factory = YamlProperySourceFactory.class)
public class YamlConfig {


}