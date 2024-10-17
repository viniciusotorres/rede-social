package rede_social.rede_social.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "POST_TB")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "CONTENT", nullable = false)
    private String content;

    @Column(name = "DISLIKES", nullable = false)
    private int dislikes;

    @Column(name = "CREATED_AT", nullable = false)
    private String createdAt;

    @OneToMany(mappedBy = "post")
    private List<Like> likes;

    @OneToMany(mappedBy = "post")
    private List<Comment> comments;

}
