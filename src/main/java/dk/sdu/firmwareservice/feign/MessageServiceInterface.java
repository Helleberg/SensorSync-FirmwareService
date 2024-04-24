package dk.sdu.firmwareservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@FeignClient("MESSAGE-SERVICE")
public interface MessageServiceInterface {
    @PostMapping("api/v1/message/update")
    @ResponseStatus(HttpStatus.OK)
    void updateFirmware(@RequestParam(name = "uuid") UUID uuid, @RequestParam(name = "jwt") String jwt);
}
