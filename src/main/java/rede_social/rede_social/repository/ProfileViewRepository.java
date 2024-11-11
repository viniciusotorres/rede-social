package rede_social.rede_social.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rede_social.rede_social.model.ProfileView;
import rede_social.rede_social.model.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ProfileViewRepository extends JpaRepository<ProfileView, Long> {
    List<ProfileView> findByViewer(User viewer);
    List<ProfileView> findByProfileOwner(User profileOwner);
    List<ProfileView> findByViewerAndProfileOwner(User viewer, User profileOwner);
    List<ProfileView> findByViewedAt(LocalDateTime viewedAt);
    List<ProfileView> findByViewerAndViewedAt(User viewer, LocalDateTime viewedAt);
    List<ProfileView> findByProfileOwnerAndViewedAt(User profileOwner, LocalDateTime viewedAt);
    List<ProfileView> findByViewerAndProfileOwnerAndViewedAt(User viewer, User profileOwner, LocalDateTime viewedAt);

    @Query("SELECT pv FROM ProfileView pv WHERE pv.profileOwner = :profileOwner AND pv.viewedAt >= :startDate")
    List<ProfileView> findByProfileOwnerAndViewedAtAfter(@Param("profileOwner") User profileOwner, @Param("startDate") LocalDateTime startDate);
}
