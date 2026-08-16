package tn.econstruction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PermitApplicationCreateDTO {

    @NotBlank(message = "{validation.workDescription.required}")
    private String workDescription;

    @NotBlank(message = "{validation.cadastralReference.required}")
    private String cadastralReference;

    private boolean collectiveConstruction = false;
}
