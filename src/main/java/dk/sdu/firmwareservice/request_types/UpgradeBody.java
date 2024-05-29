package dk.sdu.firmwareservice.request_types;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpgradeBody {
    public String token;
    public String host_ip;
    public String wifi_ssid;
    public String wifi_password;
}
