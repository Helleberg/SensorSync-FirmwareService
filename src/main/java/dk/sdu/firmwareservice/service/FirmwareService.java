package dk.sdu.firmwareservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.sdu.firmwareservice.feign.DeviceServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FirmwareService {
    private final ObjectMapper objectMapper;

    @Autowired
    DeviceServiceInterface deviceServiceInterface;

    public FirmwareService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String updateFirmware(String firmware, String deviceUUID) {
        // TODO: Get device DTO and check the current firmware version

        // TODO: If the current firmware version is outdated - Run the MakeFile with the new firmware version

        // TODO: Serve the new firmware .bin (Should maybe not be done here)

        return "Empty";
    }
}
