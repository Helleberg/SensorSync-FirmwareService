package dk.sdu.firmwareservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import dk.sdu.firmwareservice.service.GitHubService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FirmwareServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FirmwareServiceApplication.class, args);
	}

}
