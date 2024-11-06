package rede_social.rede_social.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rede_social.rede_social.model.Dislike;

import java.util.List;

public interface DislikeRepository extends JpaRepository<Dislike, Long> {
    List<Dislike> findByPostId(Long postId);
    List<Dislike> findByUserId(Long userId);
    Dislike findByUserIdAndPostId(Long userId, Long postId);
    void deleteByUserIdAndPostId(Long userId, Long postId);
}
