package tn.econstruction.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.econstruction.dto.RegistrationDTO;
import tn.econstruction.entity.Citizen;
import tn.econstruction.entity.Municipality;
import tn.econstruction.entity.User;

import java.util.Map;

/**
 * Conversion User/Citizen (entity) <-> RegistrationDTO et réponse de connexion.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Construit un Citizen à partir du formulaire d'inscription.
     * Le mot de passe est fourni déjà encodé (l'encodage est une responsabilité
     * du service, pas du mapper), de même que la commune, déjà résolue en base.
     */
    @Mapping(target = "password", source = "encodedPassword")
    @Mapping(target = "municipality", source = "municipality")
    @Mapping(target = "role", expression = "java(tn.econstruction.enums.Role.CITIZEN)")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "applications", ignore = true)
    Citizen toEntity(RegistrationDTO dto, Municipality municipality, String encodedPassword);

    /** Réponse renvoyée par /api/auth/login. */
    default Map<String, Object> toLoginResponse(User user) {
        return Map.of(
                "id", user.getId(),
                "cin", user.getCin(),
                "lastName", user.getLastName(),
                "firstName", user.getFirstName(),
                "email", user.getEmail(),
                "role", user.getRole(),
                "municipalityId", user.getMunicipality().getId(),
                "municipalityName", user.getMunicipality().getName(),
                "preferredLanguage", user.getPreferredLanguage()
        );
    }
}
