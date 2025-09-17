package lt.nsg.scroogebank.controller;

import jakarta.transaction.Transactional;
import lt.nsg.scroogebank.dto.CreateUserResponse;
import lt.nsg.scroogebank.entity.UserRoles;
import lt.nsg.scroogebank.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController()
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/create")
    @Transactional(Transactional.TxType.REQUIRED)
    public CreateUserResponse createUser(@RequestParam(value = "role", defaultValue = "user") UserRoles userRole) {
        return this.userService.createUser(userRole);
    }
}
