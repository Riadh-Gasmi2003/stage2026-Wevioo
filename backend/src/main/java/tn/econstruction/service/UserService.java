package tn.econstruction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.econstruction.dto.RegistrationDTO;
import tn.econstruction.entity.Citizen;
import tn.econstruction.entity.Municipality;
import tn.econstruction.entity.User;
import tn.econstruction.exception.BusinessException;
import tn.econstruction.mapper.UserMapper;
import tn.econstruction.repository.MunicipalityRepository;
import tn.econstruction.repository.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final MunicipalityRepository municipalityRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    public Citizen registerCitizen(RegistrationDTO dto) {
        if (userRepository.existsByCin(dto.getCin())) {
            throw new BusinessException("error.user.cinAlreadyRegistered");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new BusinessException("error.user.emailAlreadyUsed");
        }

        Municipality municipality = municipalityRepository.findById(dto.getMunicipalityId())
                .orElseThrow(() -> new BusinessException("error.municipality.notFound"));

        Citizen citizen = userMapper.toEntity(
                dto, municipality, passwordEncoder.encode(dto.getPassword()));

        return (Citizen) userRepository.save(citizen);
    }

    public User login(String cin, String password) {
        return userRepository.findByCin(cin)
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .orElseThrow(() -> new BusinessException("error.auth.invalidCredentials"));
    }

    /**
     * Met à jour la langue préférée de l'utilisateur (mémorisée d'une connexion à l'autre).
     * Seules "fr" et "ar" sont acceptées pour l'instant.
     */
    public User updatePreferredLanguage(Long userId, String language) {
        if (!"fr".equals(language) && !"ar".equals(language)) {
            throw new BusinessException("error.user.unsupportedLanguage", language);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("error.user.notFound"));
        user.setPreferredLanguage(language);
        return userRepository.save(user);
    }
}
