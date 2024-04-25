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

    @GetMapping("/firmware/latest")
    @ResponseStatus(HttpStatus.OK)
    public String getLatestVersion() {
        return gitHubService.getLatestToitRelease();
    }

    @PostMapping("/firmware/update/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    public void updateDeviceFirmware(@PathVariable("uuid") UUID uuid, @RequestBody String token) {
        firmwareService.updateFirmware(uuid, token);
    }

    // GET ALL FIRMWARE RELEASES
    @GetMapping("/firmware/toit/releases")
    @ResponseStatus(HttpStatus.OK)
    public void getAllFirmwareVersion() {
        // TODO: Implement logic that returns all version from a database or the repo releases.
    }
}
