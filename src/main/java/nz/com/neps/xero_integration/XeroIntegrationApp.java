package nz.com.neps.xero_integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAutoConfiguration
@SpringBootApplication
public class XeroIntegrationApp 
{
	public static void main(String[] args) {
		SpringApplication.run(XeroIntegrationApp.class,args);
	}

}

