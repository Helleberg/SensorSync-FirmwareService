package dk.sdu.firmwareservice;

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
