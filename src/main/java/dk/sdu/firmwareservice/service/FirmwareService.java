package dk.sdu.firmwareservice.service;

import dk.sdu.firmwareservice.dto.DeviceDTO;
import dk.sdu.firmwareservice.feign.DeviceServiceInterface;
import dk.sdu.firmwareservice.feign.MessageServiceInterface;
import dk.sdu.firmwareservice.request_types.TokenBody;
import dk.sdu.firmwareservice.request_types.UpdateFirmwareRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
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

    public void updateFirmware(UUID uuid, TokenBody token) {
        // Get device information from device service.
        DeviceDTO device = getDeviceData(uuid);

        // Get the latest toit firmware version.
        String latestToitVersion = latestToitVersion();

        // Check if latest toit firmware version is newer than the device firmware.
        if (formatFirmwareVersion(latestToitVersion) > formatFirmwareVersion(device.getToit_firmware_version())) {
            // Generate the firmware update file
            Boolean isFirmwareGenerated = generateFirmware(latestToitVersion);
            if (isFirmwareGenerated) {
                // When the firmware has been generated send a message through the MessageService
                // to allow the device to begin updating.

                // Generate the firmware update request
                UpdateFirmwareRequest request = new UpdateFirmwareRequest();
                request.setFirmware_version(latestToitVersion);
                request.setUuid(uuid);
                request.setToken(token.getToken());
                messageServiceInterface.updateFirmware(request);
            }
        }
    }

    // this expects the following version format: v2.0.0-alpha.146
    private Integer formatFirmwareVersion(String firmwareVersion) {
        return Integer.parseInt(firmwareVersion.substring(firmwareVersion.lastIndexOf('.') + 1));
    }

    private DeviceDTO getDeviceData(UUID uuid) {
        return deviceServiceInterface.getDevice(uuid);
    }

    private String latestToitVersion() {
        return gitHubService.getLatestToitRelease();
    }

    public Boolean generateFirmware(String firmwareVersion) {
        // TODO: Include ATHENA snapshot somewhere in this logic
        // Right know the latest ATHEA version just gets bundled with the new firmware.
        try {
            // Concatenating the deviceUUID onto the filename to keep track of which device should download it.
            String envelopeUrl = "https://github.com/toitlang/toit/releases/download/" + firmwareVersion + "/firmware-esp32.gz";
            downloadFile(envelopeUrl, firmwareVersion);
            gunzipFile(firmwareVersion);
            generateFirmwareBin(firmwareVersion);
            return true;
        } catch (IOException e) {
            log.warn(e.getMessage());
            return false;
        }
    }

    private static void downloadFile(String urlStr, String firmwareVersion) throws IOException {
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream("toit_firmware/" + firmwareVersion + "/firmware.envelope.gz")) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    private static void gunzipFile(String firmwareVersion) throws IOException {
        try (FileInputStream fis = new FileInputStream("toit_firmware/" + firmwareVersion + "/firmware.envelope.gz");
             GZIPInputStream gis = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream("toit_firmware/" + firmwareVersion + "/firmware.envelope")) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    private static void generateFirmwareBin(String firmwareVersion) throws IOException {
        // Create file for the firmware
        ProcessBuilder processBuilderNewDir = new ProcessBuilder();
        processBuilderNewDir.command("mkdir "+ firmwareVersion);
        processBuilderNewDir.directory(new File(System.getProperty("user.dir") + "/toit_firmware"));

        try {
            Process process = processBuilderNewDir.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Create dir: " + firmwareVersion);
            } else {
                System.out.println("Could not create dir - exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            System.err.println("Creation interrupted: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Run make file to compile
        System.out.println("Trying to compile...");
        ProcessBuilder processBuilder = new ProcessBuilder();
        String firmwarePath = "/usr/src/service/toit_firmware/" + firmwareVersion;
        processBuilder.command("make NEW_FIRMWARE_LOCATION=" + firmwarePath);
        processBuilder.directory(new File(System.getProperty("user.dir")));

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Compilation successful");
            } else {
                System.out.println("Compilation failed with exit code: " + exitCode);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            System.err.println("Compilation interrupted: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
