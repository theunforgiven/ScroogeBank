package lt.nsg.scroogebank.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateUserResponse {
    private final UUID id;
    private final UUID apiKey;
}
