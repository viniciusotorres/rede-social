package rede_social.rede_social.dto.feed;

import rede_social.rede_social.model.Post;

public record PostScore(Post post, int score) {
    public int getScore() {
        return score;
    }

    public Post getPost() {
        return post;
    }
}
