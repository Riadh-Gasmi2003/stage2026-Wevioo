package tn.econstruction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateLanguageDTO {

    @NotBlank
    private String language;
}
