package dk.sdu.firmwareservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.UUID;

@Service
public class FileProcessingServiceImpl implements FileProcessingService {
    private static final Logger log = LoggerFactory.getLogger(FirmwareService.class);

    @Override
    public Resource downloadFirmware(UUID uuid) {
        // TODO: Use the UUID when files are stored in /uuid (The file should be uuid.bin not ota.bin)
        File dir = new File("toit_firmware/" + uuid + "/ota.bin");

        try {
            if (dir.exists()) {
                log.info("Found firmware DIR");
                return new UrlResource(dir.toURI());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return null;
        }
        return null;
    }

    @Override
    public Resource deleteFirmware() {
        // TODO: Delete installed firmware from the server.
        return null;
    }
}
