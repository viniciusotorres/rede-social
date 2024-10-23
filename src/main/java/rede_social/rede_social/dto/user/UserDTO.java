package rede_social.rede_social.dto.user;

import rede_social.rede_social.model.User;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public record UserDTO(Long id, String email, String name, String birthdate, String bio, String photo, int followersCount, int followingCount) {

    public UserDTO(User user) {
        this(user.getId(), user.getEmail(), user.getName(), user.getBirthdate(), user.getBio(), Base64.getEncoder().encodeToString(user.getPhoto()), user.getFollowersCount(), user.getFollowingCount());
    }


    public static Iterable<UserDTO> fromUsers(List<User> users) {
        return Arrays.stream(users.toArray(new User[0]))
                .map(UserDTO::new)
                .toList();
    }
}