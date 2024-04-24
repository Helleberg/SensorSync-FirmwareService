package dk.sdu.firmwareservice.feign;

import dk.sdu.firmwareservice.request_types.UpdateFirmwareRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@FeignClient("MESSAGE-SERVICE")
public interface MessageServiceInterface {
    @PostMapping("api/v1/mqtt/device/update")
    @ResponseStatus(HttpStatus.OK)
    void updateFirmware(@RequestBody UpdateFirmwareRequest updateFirmwareRequest);
}
