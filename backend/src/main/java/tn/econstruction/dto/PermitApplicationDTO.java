package tn.econstruction.dto;

import lombok.Data;
import tn.econstruction.enums.ApplicationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PermitApplicationDTO {

    private Long id;
    private String applicationNumber;
    private LocalDateTime submissionDate;
    private ApplicationStatus status;
    private String workDescription;
    private Double floorArea;
    private Integer numberOfFloors;
    private String cadastralReference;
    private LocalDate legalDeadline;
    private int remainingDays;
    private int requestCount;
    private String rejectionReason;
    private boolean rejectionNoticeGenerated;
    private String agentComment;
    private String municipalityName;
    private String governorate;
    private String citizenLastName;
    private String citizenFirstName;
    private String citizenEmail;
    private boolean permitGenerated;
}
