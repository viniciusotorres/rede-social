package rede_social.rede_social.service.feed;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rede_social.rede_social.dto.feed.CommentDTO;
import rede_social.rede_social.dto.feed.FeedDTO;
import rede_social.rede_social.dto.feed.LikeDTO;
import rede_social.rede_social.dto.feed.PostDTO;
import rede_social.rede_social.model.Comment;
import rede_social.rede_social.model.Like;
import rede_social.rede_social.model.Post;
import rede_social.rede_social.model.User;
import rede_social.rede_social.repository.CommentRepository;
import rede_social.rede_social.repository.LikeRepository;
import rede_social.rede_social.repository.PostRepository;
import rede_social.rede_social.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    public FeedService(PostRepository postRepository, UserRepository userRepository, LikeRepository likeRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
    }

    public ResponseEntity<FeedDTO> getRecentPosts(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<User> followedUsers = user.getFollowing().stream().map(follow -> follow.getFollowed()).toList();
        List<Post> feedPosts = postRepository.findRecentPostsByFollowedUsers(followedUsers);
        List<PostDTO> postDTOs = feedPosts.stream()
                .map(post -> new PostDTO(
                        post.getId(),
                        post.getUser().getId(),
                        post.getContent(),
                        post.getDislikes(),
                        post.getCreatedAt(),
                        post.getLikes().stream()
                                .map(like -> new LikeDTO(like.getId(), like.getUser().getId()))
                                .collect(Collectors.toList()),
                        post.getComments().stream()
                                .map(comment -> new CommentDTO(comment.getId(), comment.getUser().getId(), comment.getContent(), comment.getCreatedAt().toString()))
                                .collect(Collectors.toList())
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new FeedDTO(postDTOs, "Posts retrieved successfully"));
    }

    public ResponseEntity<String> createPost(Long userId, String content) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = new Post();
        post.setUser(user);
        post.setContent(content);
        post.setCreatedAt(String.valueOf(LocalDateTime.now()));
        postRepository.save(post);
        return ResponseEntity.ok("Post created successfully");
    }

    public ResponseEntity<String> likePost(Long userId, Long postId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        Like like = new Like();
        like.setUser(user);
        like.setPost(post);
        likeRepository.save(like);
        return ResponseEntity.ok("Post liked successfully");
    }

    public ResponseEntity<String> commentOnPost(Long userId, Long postId, String content) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        commentRepository.save(comment);
        return ResponseEntity.ok("Comment added successfully");
    }

    public ResponseEntity<String> unlikePost(Long userId, Long postId) {
        Like like = likeRepository.findByUserIdAndPostId(userId, postId);
        likeRepository.delete(like);
        return ResponseEntity.ok("Like removed successfully");
    }

    public ResponseEntity<String> deleteComment(Long userId, Long postId, Long commentId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new RuntimeException("Comment not found"));
        if (comment.getUser().getId() != userId) {
            throw new RuntimeException("User does not have permission to delete this comment");
        }
        commentRepository.delete(comment);
        return ResponseEntity.ok("Comment deleted successfully");
    }

    public ResponseEntity<String> deletePost(Long userId, Long postId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (post.getUser().getId() != userId) {
            throw new RuntimeException("User does not have permission to delete this post");
        }
        postRepository.delete(post);
        return ResponseEntity.ok("Post deleted successfully");
    }
}