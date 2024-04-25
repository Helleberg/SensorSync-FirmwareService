package dk.sdu.firmwareservice.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.UUID;

@Service
public class FileProcessingServiceImpl implements FileProcessingService {
    @Override
    public Resource downloadFirmware(UUID uuid) {
        // TODO: Use the UUID when files are stored in /uuid
        File dir = new File("ota.bin");

        try {
            if (dir.exists()) {
                return new UrlResource(dir.toURI());
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
            return null;
        }
        return null;
    }
}
