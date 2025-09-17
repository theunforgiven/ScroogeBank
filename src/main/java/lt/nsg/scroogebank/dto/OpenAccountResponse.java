package lt.nsg.scroogebank.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class OpenAccountResponse {
    private final UUID userId;
    private final UUID accountId;
    private final boolean isActive;
}

