package dk.sdu.firmwareservice.request_types;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFirmwareRequest {
    public String firmwareVersion;
    public UUID deviceUUID;
    public String jwt;
}
