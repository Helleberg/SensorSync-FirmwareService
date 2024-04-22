package dk.sdu.firmwareservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import dk.sdu.firmwareservice.dto.DeviceDTO;
import dk.sdu.firmwareservice.feign.DeviceServiceInterface;
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
    @Value("${toit.sdk.path}")
    private static String TOIT_SDK;
    @Value("${toit.sdk.compiler}")
    private static String TOIT_COMPILE;
    @Value("${toit.sdk.firmware.tool}")
    private static String TOIT_FIRMWARE;
    @Value("${toit.sdk.firmware.download.url}")
    private static String ENVELOPE_URL_BASE;

    @Autowired
    private GitHubService gitHubService;

    @Autowired
    private DeviceServiceInterface deviceServiceInterface;

    public String updateFirmware(String firmware, UUID deviceUUID) {
        String toitVersion = "";

        // Get device from device service
        try {
            DeviceDTO deviceDTO = deviceServiceInterface.getDevice(deviceUUID);

            // Get the latest release version from toit repo
            try {
                // Check if update version is given by the user
                if (!firmware.isEmpty()) {
                    toitVersion = firmware;
                } else {
                    toitVersion = gitHubService.getLatestRelease("toitlang/toit");
                }

                if (toitVersion != null && !toitVersion.isEmpty() && deviceUUID != null) {

                    // This check relies on Toit not changing their version syntax.
                    // Might need to be checked and made in another way for stability of this system.
                    if (Integer.parseInt(toitVersion.substring(1, toitVersion.lastIndexOf("."))) > Integer.parseInt(deviceDTO.getToit_firmware_version().substring(1, deviceDTO.getToit_firmware_version().lastIndexOf(".")))) {
                        Boolean isFirmwareGenerated = generateFirmware(toitVersion);

                        if (isFirmwareGenerated) {
                            // TODO: implement logic to serve the firmware to the esp32 with the correct device UUID.
                            // "Firmware was generated successfully" return is temp and will be removed when sering the firmware is implemented.
                            return "Firmware was generated successfully";
                        } else {
                            return "Firmware not generated correctly";
                        }
                    } else {
                        return "Current device does not need to update firmware";
                    }

                } else {
                    return "Update information is missing...";
                }

            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            return "Could not get device";
        }
    }

    public Boolean generateFirmware(String firmwareVersion) {
        // TODO: Include ATHENA snapshot somewhere in this logic
        try {
            String envelopeUrl = ENVELOPE_URL_BASE + firmwareVersion + "/firmware-esp32.gz";
            downloadFile(envelopeUrl, "firmware.envelope.gz");
            gunzipFile("firmware.envelope.gz", "firmware.envelope");
            compileToitFile("validate.toit", "validate.snapshot");
            installContainerToFirmware("firmware.envelope", "validate.snapshot");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
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
        }
    }

    private static void compileToitFile(String inputFile, String outputFile) throws IOException {
        Process process = Runtime.getRuntime().exec(TOIT_COMPILE + " -w " + outputFile + " " + inputFile);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void installContainerToFirmware(String envelopeFile, String snapshotFile) throws IOException {
        Process process = Runtime.getRuntime().exec(
                TOIT_FIRMWARE + " -e " + envelopeFile + " container install validate " + snapshotFile);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
