package rede_social.rede_social.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;
import rede_social.rede_social.dto.auth.UserRegisterDTO;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "USERS_TB")
@JsonIgnoreProperties({"following", "followers"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email;

    @Column(name = "PASSWORD", nullable = false)
    private String password;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "BIRTHDATE", nullable = false)
    private String birthdate;

    @Column(name =  "BIO", nullable = true)
    private String bio;

    @Column(name = "PHOTO", nullable = false)
    private String photo;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_verified")
    private boolean isVerified;

    @OneToMany(mappedBy = "follower")
    private List<Follow> following;

    @OneToMany(mappedBy = "followed")
    private List<Follow> followers;

    public void updateFromDTO(UserRegisterDTO userRegister, PasswordEncoder passwordEncoder) {
        this.email = userRegister.email();
        this.password = passwordEncoder.encode(userRegister.password());
        this.name = userRegister.name();
        this.birthdate = userRegister.birthdate();
        this.photo = userRegister.photo();
        this.bio = userRegister.bio() != null ? userRegister.bio() : "";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isPasswordValid(String rawPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, this.password);
    }

    public int getFollowersCount() {
        return followers.size();
    }

    public int getFollowingCount() {
        return following.size();
    }
}
