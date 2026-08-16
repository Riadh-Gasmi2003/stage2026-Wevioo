package tn.econstruction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "{validation.cin.loginRequired}")
    @Pattern(regexp = "\\d{8}", message = "{validation.cin.loginFormat}")
    private String cin;

    @NotBlank(message = "{validation.password.loginRequired}")
    private String password;
}
