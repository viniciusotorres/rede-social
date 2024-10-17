package rede_social.rede_social.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rede_social.rede_social.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPostId(Long postId);
}
