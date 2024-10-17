package rede_social.rede_social.controller.feed;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rede_social.rede_social.dto.feed.FeedDTO;
import rede_social.rede_social.service.feed.FeedService;


@RestController
    @RequestMapping("/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<FeedDTO> getRecentPosts(@PathVariable Long userId) {
        return feedService.getRecentPosts(userId);
    }

    @PostMapping("/{userId}/post")
    public ResponseEntity<String> createPost(@PathVariable Long userId, @RequestBody String content) {
        return feedService.createPost(userId, content);
    }

    @PostMapping("/{userId}/post/{postId}/like")
    public ResponseEntity<String> likePost(@PathVariable Long userId, @PathVariable Long postId) {
        return feedService.likePost(userId, postId);
    }

    @PostMapping("/{userId}/post/{postId}/comment")
    public ResponseEntity<String> commentOnPost(@PathVariable Long userId, @PathVariable Long postId, @RequestBody String content) {
        return feedService.commentOnPost(userId, postId, content);
    }

    @DeleteMapping("/{userId}/post/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable Long userId, @PathVariable Long postId) {
        return feedService.deletePost(userId, postId);
    }

    @DeleteMapping("/{userId}/post/{postId}/like")
    public ResponseEntity<String> unlikePost(@PathVariable Long userId, @PathVariable Long postId) {
        return feedService.unlikePost(userId, postId);
    }

    @DeleteMapping("/{userId}/post/{postId}/comment/{commentId}")
    public ResponseEntity<String> deleteComment(@PathVariable Long userId, @PathVariable Long postId, @PathVariable Long commentId) {
        return feedService.deleteComment(userId, postId, commentId);
    }
}