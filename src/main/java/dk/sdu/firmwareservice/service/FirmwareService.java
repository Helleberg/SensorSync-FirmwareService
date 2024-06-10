package dk.sdu.firmwareservice.service;

import dk.sdu.firmwareservice.dto.DeviceDTO;
import dk.sdu.firmwareservice.feign.DeviceServiceInterface;
import dk.sdu.firmwareservice.feign.MessageServiceInterface;
import dk.sdu.firmwareservice.request_types.UpgradeBody;
import dk.sdu.firmwareservice.request_types.UpdateFirmwareRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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

    @Autowired
    static FileProcessingService fileProcessingService;

    public void updateFirmware(UUID uuid, UpgradeBody upgradeBody) {
        // Get device information from device service.
        DeviceDTO device = getDeviceData(uuid);

        // Get the latest toit firmware version.
        String latestToitVersion = latestToitVersion();

        // Check if latest toit firmware version is newer than the device firmware.
        if (formatFirmwareVersion(latestToitVersion) > formatFirmwareVersion(device.getToit_firmware_version())) {
            // Generate the firmware update file
            Boolean isFirmwareGenerated = generateFirmware(latestToitVersion, uuid, upgradeBody.getWifi_ssid(), upgradeBody.getWifi_password(), upgradeBody.getHost_ip());
            if (isFirmwareGenerated) {
                // When the firmware has been generated send a message through the MessageService
                // to allow the device to begin updating.

                // Generate the firmware update request
                UpdateFirmwareRequest request = new UpdateFirmwareRequest();
                request.setFirmware_version(latestToitVersion);
                request.setUuid(uuid);
                request.setToken(upgradeBody.getToken());
                messageServiceInterface.updateFirmware(request);
            }
        }
    }

    public static void athenaConfig (String hostIP) {
        int brokerPort = 1883;
        int gatewayPort = 8285;
        String brokerUser = "admin";
        String brokerPass = "password";
        int onBoardLedPin = 17;

        String filePath = "/usr/src/service/config.toit";
        String content = String.format(
                "HOST ::= \"%s\"\n" +
                "BROKER_PORT ::= %d\n" +
                "GATEWAY_PORT ::= %d\n" +
                "BROKER_USER ::= \"%s\"\n" +
                "BROKER_PASS ::= \"%s\"\n" +
                "ON_BOARD_LED_PIN ::= %d\n",
                hostIP, brokerPort, gatewayPort, brokerUser, brokerPass, onBoardLedPin
        );

        try {
            // Write the string to the file
            Files.write(Paths.get(filePath), content.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            log.info("Toit Config written successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            log.warn("Failed to write Toit Config.");
        }
    }

    public static void storeWiFiCredentials (String wifiSSID, String wifiPassword) {
        String jsonContent = String.format(
                "{ \"wifi\": { \"wifi.ssid\": \"%s\", \"wifi.password\": \"%s\" } }", wifiSSID, wifiPassword);

        String filePath = "/usr/src/service/wifi.json";

        try {
            Files.write(Paths.get(filePath), jsonContent.getBytes());
            log.info("WiFi file saved successfully.");
        } catch (IOException e) {
            e.printStackTrace();
            log.warn("WiFi file saving failed.");
        }
    }

    public Boolean generateFirmware(String firmwareVersion, UUID uuid, String wifiSSID, String wifiPassword, String hostIP) {
        // TODO: Include ATHENA snapshot somewhere in this logic
        // Right know the latest ATHENA version just gets bundled with the new firmware.
        try {
            // Check if the Firmware services Toit version is the latest
            // Download new one if not
            // TODO: Add this method updateFrimwareServiceToitVersion
            // updateFrimwareServiceToitVersion(firmwareVersion);

            // Remove old firmware folder if it exists
            // TODO: Add this method fileProcessingService.deleteFirmware
            // fileProcessingService.deleteFirmware("toit_firmware/" + uuid);

            // Concatenating the deviceUUID onto the filename to keep track of which device should download it.
            athenaConfig(hostIP);
            storeWiFiCredentials(wifiSSID, wifiPassword);
            String envelopeUrl = "https://github.com/toitlang/toit/releases/download/" + firmwareVersion + "/firmware-esp32.gz";
            makeFirmwareFolder(uuid);
            downloadFile(envelopeUrl, "toit_firmware/" + uuid + "/firmware.envelope.gz");
            gunzipFile("toit_firmware/" + uuid + "/firmware.envelope.gz", "toit_firmware/" + uuid + "/firmware.envelope");
            generateFirmwareBin(uuid);

            // TODO: Only return true if all of the above was successful.
            return true;
        } catch (IOException e) {
            log.warn(e.getMessage());
            return false;
        }
    }

    private static void updateFrimwareServiceToitVersion(String firmwareVersion) throws IOException {
        File dir = new File("toit");

        if (dir.exists() && dir.isDirectory()) {
            if (getFrimwareServiceToitVersion() != null) {
                if (formatFirmwareVersion(firmwareVersion) > formatFirmwareVersion(getFrimwareServiceToitVersion())) {
                    // Delete old version
                    fileProcessingService.deleteFirmware("toit");

                    // Download new version
                    downloadFile("https://github.com/toitlang/toit/releases/download/" + firmwareVersion + "/toit-linux.tar.gz", "/toit/toit-linux.tar.gz");
                    log.info("Firmware service new Toit version download");

                    // Extract new version
                    gunzipFile("toit/toit-linux.tar.gz", "toit/");
                    log.info("Firmware service new Toit version unzip and ready to use");
                }
            } else {
                log.warn("Could not find Service Toit version file");
            }
        } else {
            log.warn("Toit dir does not exist");
        }

    }

    private static String getFrimwareServiceToitVersion() {
        try (BufferedReader br = new BufferedReader(new FileReader("toit/VERSION"))) {
            if (br.readLine() != null) {
                log.info("Firmware Service Toit version: {}", br.readLine());
                return br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static void makeFirmwareFolder(UUID uuid) {
        // Create file for the firmware
        ProcessBuilder processBuilderNewDir = new ProcessBuilder();
        processBuilderNewDir.command("mkdir", uuid.toString());
        processBuilderNewDir.directory(new File("/usr/src/service/toit_firmware"));

        log.info("Creating firmware folder for device: {}", uuid);
        try {
            Process process = processBuilderNewDir.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("Create dir for device: {}", uuid);
            } else {
                log.error("Could not create dir for device: {} - with exit code: {}", uuid, exitCode);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            log.error("Creation interrupted for device: {} - with error: {}", uuid, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void downloadFile(String urlStr, String outputPath) throws IOException {
        log.info("Downloading firmware file");

        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        try (InputStream in = connection.getInputStream();
             FileOutputStream out = new FileOutputStream(outputPath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            log.info("Firmware file downloaded");
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    private static void gunzipFile(String inputFile, String output) throws IOException {
        log.info("Unzipping firmware file");

        try (FileInputStream fis = new FileInputStream(inputFile);
             GZIPInputStream gis = new GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(output)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = gis.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            log.info("Firmware file unzipped");
        } catch (Exception e) {
            log.warn(e.getMessage());
        }
    }

    private static void generateFirmwareBin(UUID uuid) throws IOException {
        // Run make file to compile
        log.info("Trying to compile... for device: {}", uuid);
        ProcessBuilder processBuilder = new ProcessBuilder();
        String firmwarePath = "/usr/src/service/toit_firmware/" + uuid.toString();
        processBuilder.command("make", "NEW_FIRMWARE_LOCATION=" + firmwarePath);
        processBuilder.directory(new File("/usr/src/service"));

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                log.info("Compilation successful for device: {}", uuid);
            } else {
                log.error("Compilation failed for device: {} - with exit code: {}", uuid, exitCode);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            log.error("Compilation interrupted for device: {} - with error: {}", uuid, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // this expects the following version format: v2.0.0-alpha.146
    private static Integer formatFirmwareVersion(String firmwareVersion) {
        return Integer.parseInt(firmwareVersion.substring(firmwareVersion.lastIndexOf('.') + 1));
    }

    private DeviceDTO getDeviceData(UUID uuid) {
        return deviceServiceInterface.getDevice(uuid);
    }

    private String latestToitVersion() {
        return gitHubService.getLatestToitRelease();
    }
}
