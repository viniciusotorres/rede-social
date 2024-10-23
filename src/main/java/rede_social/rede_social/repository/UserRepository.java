package rede_social.rede_social.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rede_social.rede_social.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByNameContainingIgnoreCase(String name);
}
