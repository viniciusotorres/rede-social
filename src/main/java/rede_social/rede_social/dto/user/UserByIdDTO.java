package rede_social.rede_social.dto.user;


import rede_social.rede_social.model.User;

public record UserByIdDTO(Long userId, String userName, int followersCount, int followingCount, String email, int postsCount) {

    public UserByIdDTO(User user, int postsCount) {
        this(user.getId(), user.getName(), user.getFollowersCount(), user.getFollowingCount(), user.getEmail(), postsCount);
    }

    public int postsCount() {
        return postsCount;
    }



}
