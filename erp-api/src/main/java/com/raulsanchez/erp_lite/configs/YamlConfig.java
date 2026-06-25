package com.raulsanchez.erp_lite.configs;

import com.raulsanchez.erp_lite.persistence.rest.models.JsonPlaceholderConfigModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.raulsanchez.erp_lite.persistence.aws.models.AwsConfigModel;
import org.springframework.context.annotation.PropertySources;

@Configuration
@EnableConfigurationProperties({
        AwsConfigModel.class,
        JsonPlaceholderConfigModel.class
})
@PropertySources({
        @PropertySource(value = "classpath:aws/aws.yaml", factory = YamlProperySourceFactory.class),
        @PropertySource(value = "classpath:jsonplaceholder/jsonplaceholder.yaml", factory = YamlProperySourceFactory.class)
})
//@PropertySource(value="classpath:aws/aws.yaml", factory = YamlProperySourceFactory.class)
public class YamlConfig {


}