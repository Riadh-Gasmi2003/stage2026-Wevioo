package tn.econstruction.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long id;
    private String cin;
    private String lastName;
    private String firstName;
    private String email;
    private String role;

    public AuthResponse(String token, Long id, String cin,
                        String lastName, String firstName, String email, String role) {
        this.token = token;
        this.id = id;
        this.cin = cin;
        this.lastName = lastName;
        this.firstName = firstName;
        this.email = email;
        this.role = role;
    }
}
