package dk.sdu.firmwareservice.feign;

import dk.sdu.firmwareservice.dto.DeviceDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@FeignClient("device-service")
public interface DeviceServiceInterface {
    @GetMapping("/devices/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    public DeviceDTO getDevice(@PathVariable UUID uuid);
}