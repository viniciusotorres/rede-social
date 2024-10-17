package rede_social.rede_social.controller.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rede_social.rede_social.dto.user.UserDTO;
import rede_social.rede_social.service.user.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("profile/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PutMapping("profile/{id}")
    public ResponseEntity<UserDTO> updateUser(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        return userService.updateUser(id, userDTO);
    }

    @PostMapping("{followerId}/follow/{followedId}")
    public ResponseEntity<String> followUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        return userService.followUser(followerId, followedId);
    }

    @DeleteMapping("{followerId}/unfollow/{followedId}")
    public ResponseEntity<String> unfollowUser(@PathVariable Long followerId, @PathVariable Long followedId) {
        return userService.unfollowUser(followerId, followedId);
    }

}
