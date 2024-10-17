package rede_social.rede_social.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rede_social.rede_social.model.ConfirmationCode;

public interface ConfirmationCodeRepository extends JpaRepository<ConfirmationCode, String> {

}
