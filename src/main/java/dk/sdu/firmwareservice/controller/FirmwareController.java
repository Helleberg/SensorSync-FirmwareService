package dk.sdu.firmwareservice.controller;

import dk.sdu.firmwareservice.dto.DeviceDTO;
import dk.sdu.firmwareservice.service.FirmwareService;
import dk.sdu.firmwareservice.service.GitHubService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("api/v1")
public class FirmwareController {
    @Autowired
    GitHubService gitHubService;
    @Autowired
    FirmwareService firmwareService;

    // GET NEWEST FIRMWARE RELEASE
    @GetMapping("/firmware/toit")
    @ResponseStatus(HttpStatus.OK)
    public String getFirmwareVersion(@RequestParam(name = "repoPath") String repoPath) {
        try {
            return gitHubService.getLatestRelease(repoPath);
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    // GET ALL FIRMWARE RELEASES
    @GetMapping("/firmware/toit")
    @ResponseStatus(HttpStatus.OK)
    public void getAllFirmwareVersion() {
        // TODO: Implement logic that returns all version from a database or the repo releases.
    }

    // UPDATE DEVICE WITH NEWEST FIRMWARE RELEASE
    @PostMapping("/firmware/toit")
    @ResponseStatus(HttpStatus.OK)
    public String updateFirmware(@RequestParam(name = "firmwareVersion") String version, @RequestParam(name = "deviceUUID") String deviceUUID) {
        try {
            return firmwareService.updateFirmware(version, deviceUUID);
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
