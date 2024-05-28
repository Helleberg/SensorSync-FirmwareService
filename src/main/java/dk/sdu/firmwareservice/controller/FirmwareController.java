package dk.sdu.firmwareservice.controller;

import dk.sdu.firmwareservice.request_types.TokenBody;
import dk.sdu.firmwareservice.service.FileProcessingService;
import dk.sdu.firmwareservice.service.FirmwareService;
import dk.sdu.firmwareservice.service.GitHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.UUID;

@RestController
@RequestMapping("api/v1")
public class FirmwareController {
    private static final Logger log = LoggerFactory.getLogger(FirmwareService.class);

    @Autowired
    FileProcessingService fileProcessingService;
    @Autowired
    GitHubService gitHubService;
    @Autowired
    FirmwareService firmwareService;

    @GetMapping("/firmware/toit/latest")
    @ResponseStatus(HttpStatus.OK)
    public String getLatestToitVersion() {
        return gitHubService.getLatestToitRelease();
    }

    @GetMapping("/firmware/athena/latest")
    @ResponseStatus(HttpStatus.OK)
    public String getLatestAthenaVersion() {
        return gitHubService.getLatestAthenaRelease();
    }

    @PostMapping("/firmware/update/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    public void updateDeviceFirmware(@PathVariable("uuid") UUID uuid, @RequestBody TokenBody token) {
        firmwareService.updateFirmware(uuid, token);
    }

    @GetMapping(value = "/firmware/download/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> downloadFirmware(@PathVariable("uuid") UUID uuid) throws IOException {
        Resource file = fileProcessingService.downloadFirmware(uuid);
        if (file == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            log.info("Returned OK (200) response with the firmware as content");
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(file.contentLength())
                    .body(file);
        }
    }

    // DELETE FIRMWARE FOLDER AFTER UPDATE
    @GetMapping("/firmware/delete/{uuid}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteFirmwareVersion(@PathVariable("uuid") UUID uuid) {
        fileProcessingService.deleteFirmware("toit_firmware/" + uuid);
    }

    // GET ALL FIRMWARE RELEASES
    @GetMapping("/firmware/toit/releases")
    @ResponseStatus(HttpStatus.OK)
    public void getAllFirmwareVersion() {
        // TODO: Implement logic that returns all version from a database or the repo releases.
        // Might not be in scope
    }
}
