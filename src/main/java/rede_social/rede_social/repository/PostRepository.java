package rede_social.rede_social.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rede_social.rede_social.model.Post;
import rede_social.rede_social.model.User;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p WHERE p.user IN :followedUsers ORDER BY p.createdAt DESC")
    List<Post> findRecentPostsByFollowedUsers(@Param("followedUsers") List<User> followedUsers);
}