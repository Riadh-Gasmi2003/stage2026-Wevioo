package tn.econstruction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectionDTO {

    // RM-012 : refus motivé obligatoire avec référence aux articles CATU non respectés
    @NotBlank(message = "{validation.rejectionReason.required}")
    private String reason;
}
