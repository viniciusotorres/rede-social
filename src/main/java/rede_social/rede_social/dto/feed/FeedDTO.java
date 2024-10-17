package rede_social.rede_social.dto.feed;


import java.util.List;

public record FeedDTO(List<PostDTO> posts, String message) {
}