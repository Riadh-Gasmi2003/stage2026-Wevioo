package tn.econstruction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.econstruction.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByCin(String cin);

    Optional<User> findByEmail(String email);

    boolean existsByCin(String cin);

    boolean existsByEmail(String email);
}
