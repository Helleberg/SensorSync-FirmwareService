package dk.sdu.firmwareservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import dk.sdu.firmwareservice.dto.DeviceDTO;
import dk.sdu.firmwareservice.feign.DeviceServiceInterface;
import dk.sdu.firmwareservice.feign.MessageServiceInterface;
import dk.sdu.firmwareservice.request_types.UpdateFirmwareRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

@Component
public class FirmwareService {
    private static final Logger log = LoggerFactory.getLogger(FirmwareService.class);

    @Autowired
    private GitHubService gitHubService;

    @Autowired
    private DeviceServiceInterface deviceServiceInterface;

    @Autowired
    private MessageServiceInterface messageServiceInterface;

    public void updateFirmware(UpdateFirmwareRequest updateFirmwareRequest) {
        String toitVersion = "";

        // Get device from device service
        try {
            DeviceDTO deviceDTO = deviceServiceInterface.getDevice(updateFirmwareRequest.getUuid());

            // Get the latest release version from toit repo
            try {
                // Check if update version is given by the user
                if (!updateFirmwareRequest.getToit_firmware_version().isEmpty()) {
                    toitVersion = updateFirmwareRequest.getToit_firmware_version();
                } else {
                    toitVersion = gitHubService.getLatestToitRelease();
                }

                if (toitVersion != null && !toitVersion.isEmpty() && updateFirmwareRequest.getUuid() != null) {

                    // This check relies on Toit not changing their version syntax.
                    // Might need to be checked and made in another way for stability of this system.
                    if (Integer.parseInt(toitVersion.substring(1, toitVersion.lastIndexOf("."))) > Integer.parseInt(deviceDTO.getToit_firmware_version().substring(1, deviceDTO.getToit_firmware_version().lastIndexOf(".")))) {
                        Boolean isFirmwareGenerated = generateFirmware(toitVersion, String.valueOf(updateFirmwareRequest.getUuid()));

                        if (isFirmwareGenerated) {
                            // TODO: implement logic to serve the firmware to the esp32 with the correct device UUID.
                            // "Firmware was generated successfully" return is temp and will be removed when sering the firmware is implemented.
                            messageServiceInterface.updateFirmware(updateFirmwareRequest.getUuid(), updateFirmwareRequest.getJwt());

                            log.warn("Firmware was generated successfully");
                        } else {
                            log.warn("Firmware not generated correctly");
                        }
                    } else {
                        log.warn("Current device does not need to update firmware");
                    }

                } else {
                    log.warn("Update information is missing...");
                }

            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            log.error("Could not get device: {}", e.getMessage());
        }
    }

    public Boolean generateFirmware(String firmwareVersion, String deviceUUID) {
        // TODO: Include ATHENA snapshot somewhere in this logic
        try {
            // Concatenating the deviceUUID onto the filename to keep track of which device should download it.
            String envelopeUrl = "https://github.com/toitlang/toit/releases/download/" + firmwareVersion + "/firmware-esp32.gz";
            downloadFile(envelopeUrl, deviceUUID + ".firmware.envelope.gz");
            gunzipFile(deviceUUID + ".firmware.envelope.gz", deviceUUID + ".firmware.envelope");
            compileToitFile("validate.toit", "validate.snapshot");
            installContainerToFirmware(deviceUUID + ".firmware.envelope", "validate.snapshot");
            return true;
        } catch (IOException e) {
            log.warn(e.getMessage());
            return false;
        }
    }

    private static void downloadFile(String urlStr, String fileName) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(fileName)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    private static void gunzipFile(String inputFileName, String outputFileName) throws IOException {
        try (FileInputStream fis = new FileInputStream(inputFileName);
             GZIPInputStream gis = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(outputFileName)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    private static void compileToitFile(String inputFile, String outputFile) throws IOException {
        Process process = Runtime.getRuntime().exec("./toit/bin/toit.compile" + " -w " + outputFile + " " + inputFile);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            log.warn(e.getMessage());
        }
    }

    private static void installContainerToFirmware(String envelopeFile, String snapshotFile) throws IOException {
        Process process = Runtime.getRuntime().exec(
                "./toit/tools/firmware" + " -e " + envelopeFile + " container install validate " + snapshotFile);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            log.warn(e.getMessage());
        }
    }
}
