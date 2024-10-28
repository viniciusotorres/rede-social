package rede_social.rede_social.dto.user;

import rede_social.rede_social.model.User;

import java.util.Arrays;
import java.util.List;

public record TopUserDTO (
        Long id,
        String name,
        int followersCount
){


    public static Object fromUsers(List<User> topUsers) {
        return Arrays.stream(topUsers.toArray(new User[0]))
                .map(user -> new TopUserDTO(user.getId(), user.getName(), user.getFollowersCount()))
                .toList();
    }

}
