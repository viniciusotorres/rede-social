package rede_social.rede_social.dto.user;

import rede_social.rede_social.model.User;

import java.util.Arrays;

public record UserDTO(Long id, String email, String name, String birthdate, String bio, String photo, int followersCount, int followingCount) {

    public UserDTO(User user) {
        this(user.getId(), user.getEmail(), user.getName(), user.getBirthdate(), user.getBio(), Arrays.toString(user.getPhoto()), user.getFollowersCount(), user.getFollowingCount());
    }
}