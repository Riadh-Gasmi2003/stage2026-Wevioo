package tn.econstruction.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.econstruction.dto.PermitApplicationCreateDTO;
import tn.econstruction.dto.PermitApplicationDTO;
import tn.econstruction.entity.PermitApplication;

/**
 * Conversion PermitApplication (entity) <-> PermitApplicationDTO / PermitApplicationCreateDTO.
 * Les champs métier (applicationNumber, submissionDate, status, legalDeadline,
 * citizen, municipality...) restent à la charge du service, car ils dépendent
 * de règles métier et non d'une simple recopie de champs.
 */
@Mapper(componentModel = "spring")
public interface PermitApplicationMapper {

    @Mapping(target = "remainingDays", expression = "java(application.calculateRemainingDays())")
    @Mapping(target = "rejectionNoticeGenerated", expression = "java(application.getRejectionNoticePdfPathFr() != null)")
    @Mapping(target = "permitGenerated", expression = "java(application.getBuildingPermit() != null)")
    @Mapping(target = "municipalityName", source = "municipality.name")
    @Mapping(target = "governorate", source = "municipality.governorate")
    @Mapping(target = "citizenLastName", source = "citizen.lastName")
    @Mapping(target = "citizenFirstName", source = "citizen.firstName")
    @Mapping(target = "citizenEmail", source = "citizen.email")
    PermitApplicationDTO toDTO(PermitApplication application);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "applicationNumber", ignore = true)
    @Mapping(target = "submissionDate", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "floorArea", ignore = true)
    @Mapping(target = "numberOfFloors", ignore = true)
    @Mapping(target = "legalDeadline", ignore = true)
    @Mapping(target = "requestCount", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "rejectionNoticePdfPathFr", ignore = true)
    @Mapping(target = "rejectionNoticePdfPathAr", ignore = true)
    @Mapping(target = "agentComment", ignore = true)
    @Mapping(target = "citizen", ignore = true)
    @Mapping(target = "municipality", ignore = true)
    @Mapping(target = "reviewingAgent", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "buildingPermit", ignore = true)
    PermitApplication toEntity(PermitApplicationCreateDTO dto);
}
