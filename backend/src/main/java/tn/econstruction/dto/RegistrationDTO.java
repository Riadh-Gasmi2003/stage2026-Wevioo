package tn.econstruction.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import tn.econstruction.enums.Role;

@Data
public class RegistrationDTO {

    @NotBlank
    @Pattern(regexp = "\\d{8}", message = "{validation.cin.registrationFormat}")
    private String cin;

    @NotBlank
    private String lastName;

    @NotBlank
    private String firstName;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "{validation.password.minLength}")
    private String password;

    @Pattern(regexp = "^$|\\+?[0-9]{8,15}", message = "{validation.phone.invalid}")
    private String phone;

    @NotNull(message = "{validation.municipality.required}")
    private Long municipalityId;

    // CITIZEN uniquement depuis le portail public
    private Role role = Role.CITIZEN;

    private String address;
    private String taxRegistrationNumber;
}
