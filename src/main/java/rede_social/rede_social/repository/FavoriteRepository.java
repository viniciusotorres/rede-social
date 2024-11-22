package rede_social.rede_social.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rede_social.rede_social.model.Favorite;
import rede_social.rede_social.model.User;

import java.util.Optional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    boolean existsByUserIdAndUserFavoriteId(Long userId, Long userFavoriteId);

    Optional<Favorite> findByUserIdAndUserFavoriteId(Long userId, Long userFavoriteId);

    Optional<Favorite> findByUserAndUserFavorite(User user, User userFavorite);
}
