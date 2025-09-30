package com.ehb.connected;

import com.ehb.connected.config.properties.ConnectedProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(
        ConnectedProperties.class
)
public class ConnectedApplication {

	public static void main(String[] args) {
		//klein testje
		SpringApplication.run(ConnectedApplication.class, args);
	}

}
