package rede_social.rede_social.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import rede_social.rede_social.dto.user.UserDTO;
import rede_social.rede_social.model.Follow;
import rede_social.rede_social.model.User;
import rede_social.rede_social.repository.FollowRepository;
import rede_social.rede_social.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    public ResponseEntity<UserDTO> getUserById(Long id) {
        try {
            var user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return ResponseEntity.ok(new UserDTO(user.getId(), user.getEmail(), user.getName(), user.getBirthdate(), user.getBio(), user.getPhoto(), user.getFollowersCount(), user.getFollowingCount()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<UserDTO> updateUser(Long id, UserDTO userDTO) {
        try {
            var user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setEmail(userDTO.email());
            user.setName(userDTO.name());
            user.setBirthdate(userDTO.birthdate());
            user.setBio(userDTO.bio());
            user.setPhoto(userDTO.photo());
            userRepository.save(user);
            return ResponseEntity.ok(new UserDTO(user.getId(), user.getEmail(), user.getName(), user.getBirthdate(), user.getBio(), user.getPhoto(), user.getFollowersCount(), user.getFollowingCount()));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<String> followUser(Long followerId, Long followedId) {
        if (followerId.equals(followedId)) {
            return ResponseEntity.badRequest().body("A user cannot follow themselves");
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found"));
        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new RuntimeException("Followed user not found"));

        boolean alreadyFollowing = followRepository.findByFollowerAndFollowed(follower, followed).isPresent();
        if (alreadyFollowing) {
            return ResponseEntity.badRequest().body("User is already following this user");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowed(followed);
        followRepository.save(follow);

        return ResponseEntity.ok("User followed successfully");
    }

    public ResponseEntity<String> unfollowUser(Long followerId, Long followedId) {
        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new RuntimeException("Follower not found"));
        User followed = userRepository.findById(followedId)
                .orElseThrow(() -> new RuntimeException("Followed user not found"));

        Follow follow = followRepository.findByFollowerAndFollowed(follower, followed)
                .orElseThrow(() -> new RuntimeException("Follow relationship not found"));
        followRepository.delete(follow);

        return ResponseEntity.ok("User unfollowed successfully");
    }
}
