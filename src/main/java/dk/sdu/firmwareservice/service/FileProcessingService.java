package dk.sdu.firmwareservice.service;

import org.springframework.core.io.Resource;

import java.util.UUID;

public interface FileProcessingService {
    Resource downloadFirmware(UUID uuid);
}
