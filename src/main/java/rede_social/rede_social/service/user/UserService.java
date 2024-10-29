package rede_social.rede_social.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import rede_social.rede_social.dto.user.TopUserDTO;
import rede_social.rede_social.dto.user.UserByIdDTO;
import rede_social.rede_social.dto.user.UserDTO;
import rede_social.rede_social.model.Follow;
import rede_social.rede_social.model.User;
import rede_social.rede_social.repository.FollowRepository;
import rede_social.rede_social.repository.PostRepository;
import rede_social.rede_social.repository.UserRepository;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class UserService {

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FollowRepository followRepository;

    public ResponseEntity<UserDTO> getUserById(Long id) {
        try {
            var user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return ResponseEntity.ok(new UserDTO(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getBirthdate(),
                    user.getBio(),
                    Base64.getEncoder().encodeToString(user.getPhoto()),
                    user.getFollowersCount(),
                    user.getFollowingCount()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<UserByIdDTO> getOnlyUser(Long id){
        try {
            var posts = postRepository.findByUser(userRepository.findById(id).get());

            var user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return ResponseEntity.ok(new UserByIdDTO(
                    user.getId(),
                    user.getName(),
                    user.getFollowersCount(),
                    user.getFollowingCount(),
                    user.getEmail(),
                    posts.size()

            ));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<Iterable<UserDTO>> getAllUsers() {
        var users = userRepository.findAll();
        return ResponseEntity.ok(UserDTO.fromUsers(users));
    }

    public ResponseEntity<List<UserDTO>> getUsersByName(String name) {
        List<User> users = userRepository.findByNameContainingIgnoreCase(name);
        return ResponseEntity.ok((List<UserDTO>) UserDTO.fromUsers(users));
    }

    public ResponseEntity<UserDTO> updateUser(Long id, UserDTO userDTO) {
        try {
            var user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setEmail(userDTO.email());
            user.setName(userDTO.name());
            user.setBirthdate(userDTO.birthdate());
            user.setBio(userDTO.bio());
            user.setPhoto(Base64.getDecoder().decode(userDTO.photo()));
            userRepository.save(user);
            return ResponseEntity.ok(new UserDTO(
                    user.getId(),
                    user.getEmail(),
                    user.getName(),
                    user.getBirthdate(),
                    user.getBio(),
                    Base64.getEncoder().encodeToString(user.getPhoto()),
                    user.getFollowersCount(),
                    user.getFollowingCount()
            ));
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

    public ResponseEntity<List<TopUserDTO>> getTopUsers() {
        List<User> users = userRepository.findAll();
        List<User> topUsers = users.stream()
                .sorted((u1, u2) -> Integer.compare(u2.getFollowersCount(), u1.getFollowersCount()))
                .limit(5)
                .collect(Collectors.toList());
        return ResponseEntity.ok((List<TopUserDTO>) TopUserDTO.fromUsers(topUsers));
    }
}
