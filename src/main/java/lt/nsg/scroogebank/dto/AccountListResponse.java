package lt.nsg.scroogebank.dto;

import lombok.Data;

import java.util.List;

@Data
public class AccountListResponse {
    private final List<AccountInformation> accounts;
}
