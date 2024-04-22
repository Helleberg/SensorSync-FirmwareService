package dk.sdu.firmwareservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceDTO {
    private UUID uuid;
    private String name;
    private boolean online;
    private String athena_version;
    private String toit_firmware_version;
    private String device_group;
    private Date last_ping;
    private Date date_created;
}
