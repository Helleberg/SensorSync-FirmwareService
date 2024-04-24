package dk.sdu.firmwareservice.controller;

import dk.sdu.firmwareservice.request_types.UpdateFirmwareRequest;
import dk.sdu.firmwareservice.service.FirmwareService;
import dk.sdu.firmwareservice.service.GitHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1")
public class FirmwareController {
    private static final Logger log = LoggerFactory.getLogger(FirmwareService.class);

    @Autowired
    GitHubService gitHubService;
    @Autowired
    FirmwareService firmwareService;

    // GET NEWEST TOIT FIRMWARE RELEASE
    @GetMapping("/firmware/toit")
    @ResponseStatus(HttpStatus.OK)
    public String getFirmwareVersion() {
        try {
            return gitHubService.getLatestToitRelease();
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    // GET ALL FIRMWARE RELEASES
    @GetMapping("/firmware/toit/releases")
    @ResponseStatus(HttpStatus.OK)
    public void getAllFirmwareVersion() {
        // TODO: Implement logic that returns all version from a database or the repo releases.
    }

    // UPDATE DEVICE WITH NEWEST FIRMWARE RELEASE
    @PostMapping("/firmware/toit")
    @ResponseStatus(HttpStatus.OK)
    public void updateFirmware(@RequestBody UpdateFirmwareRequest updateFirmwareRequest) {
        try {
            firmwareService.updateFirmware(
                    updateFirmwareRequest.getFirmwareVersion(),
                    updateFirmwareRequest.getDeviceUUID(),
                    updateFirmwareRequest.getJwt()
            );
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    // GET ALL FIRMWARE RELEASES
    @GetMapping("/firmware/toit/download")
    @ResponseStatus(HttpStatus.OK)
    public void getNewFirmware() {
        // TODO: Implement logic that serves the firmware file to the correct ESP32 device.
    }
}
