package dk.sdu.firmwareservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;

@Service
public class FileProcessingServiceImpl implements FileProcessingService {
    private static final Logger log = LoggerFactory.getLogger(FirmwareService.class);

    @Override
    public Resource downloadFirmware(UUID uuid) {
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
    public void deleteFirmware(String path) {
        File dir = new File(path);

        try {
            if (dir.exists() && dir.isDirectory()) {
                deleteDirectoryRecursively(dir.toPath());
                log.info("Successfully deleted firmware directory: {}", dir.getAbsolutePath());
            } else {
                log.warn("Firmware directory does not exist: {}", dir.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Error deleting firmware directory: {}", dir.getAbsolutePath(), e);
        }
    }

    private void deleteDirectoryRecursively(Path path) throws Exception {
        Files.walk(path)
                .sorted(Comparator.reverseOrder()) // Sort in reverse order to delete files before directories
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (Exception e) {
                        throw new RuntimeException("Error deleting file: " + p.toString(), e);
                    }
                });
    }
}
