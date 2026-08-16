package tn.econstruction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdditionalDocumentsRequestDTO {

    @NotBlank(message = "{validation.agentComment.required}")
    private String comment;   // précise quelles pièces sont manquantes + ref. articles CATU
}
