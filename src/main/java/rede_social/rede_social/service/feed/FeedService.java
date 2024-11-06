package rede_social.rede_social.service.feed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rede_social.rede_social.dto.feed.*;
import rede_social.rede_social.model.*;
import rede_social.rede_social.repository.*;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Serviço responsável pela manipulação de posts, comentários e likes.
 */
@Service
public class FeedService {

    private static final Logger logger = LoggerFactory.getLogger(FeedService.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final DislikeRepository dislikeRepository;

    public FeedService(PostRepository postRepository, UserRepository userRepository,
                       LikeRepository likeRepository, CommentRepository commentRepository, DislikeRepository dislikeRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.likeRepository = likeRepository;
        this.commentRepository = commentRepository;
        this.dislikeRepository = dislikeRepository;
    }

    /**
     * Obtém os posts mais recentes do usuário e de seus seguidores.
     *
     * @param userId ID do usuário
     * @return FeedDTO com os posts recentes
     */
    @Transactional(readOnly = true)
    public ResponseEntity<FeedDTO> getRecentPosts(Long userId) {
        User user = findUserById(userId);
        List<User> followedUsers = user.getFollowing().stream()
                .map(follow -> follow.getFollowed())
                .toList();

        List<Post> feedPosts = getFeedPosts(followedUsers, user);

        List<PostDTO> postDTOs = convertToPostDTOs(feedPosts);
        return ResponseEntity.ok(new FeedDTO(postDTOs, "Posts recuperados com sucesso"));
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Usuário não encontrado: {}", userId);
                    return new RuntimeException("Usuário não encontrado");
                });
    }

    private List<Post> getFeedPosts(List<User> followedUsers, User user) {
        List<Post> followedUsersPosts = postRepository.findRecentPostsByFollowedUsers(followedUsers);
        List<Post> userPosts = postRepository.findRecentPostsByUser(user);
        followedUsersPosts.addAll(userPosts);
        return followedUsersPosts;
    }

    private List<PostDTO> convertToPostDTOs(List<Post> posts) {
        return posts.stream()
                .map(this::convertToPostDTO)
                .collect(Collectors.toList());
    }

    private PostDTO convertToPostDTO(Post post) {
        return new PostDTO(
                post.getId(),
                post.getUser().getId(),
                post.getUser().getName(),
                Base64.getEncoder().encodeToString(post.getUser().getPhoto()),
                post.getContent(),
                convertDislikesToDTOs(post.getDislikes()),
                post.getCreatedAt(),
                convertLikesToDTOs(post.getLikes()),
                convertCommentsToDTOs(post.getComments())
        );
    }

    private List<LikeDTO> convertLikesToDTOs(List<Like> likes) {
        return likes.stream()
                .map(like -> new LikeDTO(like.getId(), like.getUser().getId()))
                .collect(Collectors.toList());
    }

    private List<CommentDTO> convertCommentsToDTOs(List<Comment> comments) {
        return comments.stream()
                .map(comment -> new CommentDTO(comment.getId(), comment.getUser().getId(),
                        comment.getContent(), comment.getCreatedAt().toString()))
                .collect(Collectors.toList());
    }

    private List<DislikeDTO> convertDislikesToDTOs(List<Dislike> dislikes) {
        return dislikes.stream()
                .map(dislike -> new DislikeDTO(dislike.getId(), dislike.getUser().getId(), dislike.getPost().getId()))
                .collect(Collectors.toList());
    }
    /**
     * Obtém os posts mais famosos com base em likes e comentários.
     *
     * @return FeedTopDTO com os posts mais famosos
     */
    public ResponseEntity<FeedTopDTO> getTopFamousPosts() {
        List<PostScore> topPosts = postRepository.findAll().stream()
                .map(this::createPostScore)
                .sorted((ps1, ps2) -> {
                    // Primeiro, comparar pelo número de dislikes (menos dislikes primeiro)
                    int dislikeComparison = Integer.compare(ps1.getPost().getDislikes().size(), ps2.getPost().getDislikes().size());
                    if (dislikeComparison != 0) {
                        return dislikeComparison;
                    }
                    // Se o número de dislikes for igual, comparar pelo score (mais curtidas e comentários primeiro)
                    return Integer.compare(ps2.getScore(), ps1.getScore());
                })
                .limit(5)
                .collect(Collectors.toList());

        List<TopPostDTO> topPostDTOs = convertToTopPostDTOs(topPosts);
        return ResponseEntity.ok(new FeedTopDTO(topPostDTOs, "Top posts recuperados com sucesso"));
    }
    private PostScore createPostScore(Post post) {
        return new PostScore(post, post.getLikes().size() + post.getComments().size());
    }

    private List<TopPostDTO> convertToTopPostDTOs(List<PostScore> postScores) {
        return postScores.stream()
                .map(postScore -> convertToTopPostDTO(postScore.getPost()))
                .collect(Collectors.toList());
    }

    private TopPostDTO convertToTopPostDTO(Post post) {
        return new TopPostDTO(
                post.getId(),
                post.getUser().getId(),
                post.getUser().getName(),
                post.getContent(),
                post.getCreatedAt(),
                convertLikesToDTOs(post.getLikes()),
                convertCommentsToDTOs(post.getComments())
        );
    }

    /**
     * Cria um novo post.
     *
     * @param userId  ID do usuário que cria o post
     * @param content Conteúdo do post
     * @return Mensagem de sucesso
     */
    public ResponseEntity<String> createPost(Long userId, String content) {
        User user = findUserById(userId);
        Post post = new Post();
        post.setUser(user);
        post.setContent(content);
        post.setDislikes(List.of());
        post.setLikes(List.of());
        post.setComments(List.of());
        post.setCreatedAt(LocalDateTime.now().toString());

        postRepository.save(post);
        logger.info("Post criado com sucesso: {}", post.getId());
        return ResponseEntity.ok("Post criado com sucesso");
    }

    /**
     * Curte um post.
     *
     * @param userId ID do usuário que curte
     * @param postId ID do post a ser curtido
     * @return Mensagem de sucesso
     */
    public ResponseEntity<String> likePost(Long userId, Long postId) {
        User user = findUserById(userId);
        Post post = findPostById(postId);


        verifyLikeNotExists(userId, postId);

        Like like = new Like();
        like.setUser(user);
        like.setPost(post);
        likeRepository.save(like);

        logger.info("Post curtido com sucesso: {} por usuário: {}", postId, userId);
        return ResponseEntity.ok("Post curtido com sucesso");
    }

    private Post findPostById(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> {
                    logger.error("Post não encontrado: {}", postId);
                    return new RuntimeException("Post não encontrado");
                });
    }

    private void verifyLikeNotExists(Long userId, Long postId) {
        Optional<Like> existingLike = likeRepository.findByUserIdAndPostId(userId, postId);
        if (existingLike.isPresent()) {
            logger.warn("Usuário já curtiu o post: {}", postId);
            throw new RuntimeException("Usuário já curtiu o post");
        }
    }

    /**
     * Remove o like de um post.
     *
     * @param userId ID do usuário que remove o like
     * @param postId ID do post do qual o like será removido
     * @return Mensagem de sucesso
     */
    public ResponseEntity<String> unlikePost(Long userId, Long postId) throws Throwable {
        Like like = (Like) likeRepository.findByUserIdAndPostId(userId, postId)
                .orElseThrow(() -> {
                    logger.error("Like não encontrado para usuário: {} e post: {}", userId, postId);
                    return new RuntimeException("Like não encontrado");
                });

        likeRepository.delete(like);
        logger.info("Like removido com sucesso: {} por usuário: {}", postId, userId);
        return ResponseEntity.ok("Like removido com sucesso");
    }

    /**
     * Comenta em um post.
     *
     * @param userId  ID do usuário que comenta
     * @param postId  ID do post que receberá o comentário
     * @param content Conteúdo do comentário
     * @return Mensagem de sucesso
     */
    public ResponseEntity<String> commentOnPost(Long userId, Long postId, String content) {
        User user = findUserById(userId);
        Post post = findPostById(postId);

        Comment comment = new Comment();
        comment.setUser(user);
        comment.setPost(post);
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);
        logger.info("Comentário adicionado com sucesso ao post: {} por usuário: {}", postId, userId);
        return ResponseEntity.ok("Comentário adicionado com sucesso");
    }

    /**
     * Remove um comentário de um post.
     *
     * @param userId    ID do usuário que remove o comentário
     * @param postId    ID do post do qual o comentário será removido
     * @param commentId ID do comentário a ser removido
     * @return Mensagem de sucesso
     */
    public ResponseEntity<String> deleteComment(Long userId, Long postId, Long commentId) {
        User user = findUserById(userId);
            Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    logger.error("Comentário não encontrado: {}", commentId);
                    return new RuntimeException("Comentário não encontrado");
                });

        if (!comment.getUser().getId().equals(userId)) {
            logger.warn("Usuário não tem permissão para deletar o comentário: {}", commentId);
            throw new RuntimeException("Usuário não tem permissão para deletar este comentário");
        }

        commentRepository.delete(comment);
        logger.info("Comentário removido com sucesso: {}", commentId);
        return ResponseEntity.ok("Comentário removido com sucesso");
    }

    /**
     * Remove um post.
     *
     * @param userId ID do usuário que remove o post
     * @param postId ID do post a ser removido
     * @return Mensagem de sucesso
     */
    public ResponseEntity<String> deletePost(Long userId, Long postId) {
        User user = findUserById(userId);
        Post post = findPostById(postId);

        if (!post.getUser().getId().equals(userId)) {
            logger.warn("Usuário não tem permissão para deletar o post: {}", postId);
            throw new RuntimeException("Usuário não tem permissão para deletar este post");
        }

        postRepository.delete(post);
        logger.info("Post removido com sucesso: {}", postId);
        return ResponseEntity.ok("Post removido com sucesso");
    }

    public ResponseEntity<LikesDTO> getLikes(Long userId, Long postId) {

        if (!postRepository.existsById(postId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Post post = findPostById(postId);

        List<Like> likes = post.getLikes();

        List<ListLikeDTO> listLikeDTO =
                likes.stream().map(like ->
                        new ListLikeDTO(like.getId(),
                                like.getUser().getId(), like.getUser().getName()
                                .toString())).collect(Collectors.toList());

        Number countLikes = likes.size();

        return ResponseEntity.ok(new LikesDTO(postId, userId, listLikeDTO, countLikes));
    }

    public ResponseEntity<CommentsDTO> getComments(Long userId, Long postId){
        if (!postRepository.existsById(postId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Post post = findPostById(postId);

        List<Comment> comments = post.getComments();

        List<ListCommentDTO> listCommentDTO =
                comments.stream().map(comment ->
                        new ListCommentDTO(comment.getId(),
                                comment.getUser().getId(), comment.getUser().getName()
                                .toString(), comment.getContent(), comment.getCreatedAt().toString())).collect(Collectors.toList());

        Number countComments = comments.size();

        return ResponseEntity.ok(new CommentsDTO(postId, userId, listCommentDTO, countComments));
    }

    public ResponseEntity<String> deslikePost(Long userId, Long postId) {
        User user = findUserById(userId);
        Post post = findPostById(postId);
        Dislike dislike = dislikeRepository.findByUserIdAndPostId(userId, postId);

        if (dislike != null) {
            logger.warn("Usuário já descurtiu o post: {}", postId);
            throw new RuntimeException("Usuário já descurtiu o post");
        }

        if (post.getLikes().stream().anyMatch(like -> like.getUser().getId().equals(userId))) {
            logger.warn("Usuário não pode não curtir um post que já curtiu. {}", postId);
            throw new RuntimeException("Usuário não pode não curtir um post que já curtiu");
        }

        Dislike newDislike = new Dislike();
        newDislike.setUser(user);
        newDislike.setPost(post);
        dislikeRepository.save(newDislike);

        logger.info("Post descurtido com sucesso: {} por usuário: {}", postId, userId);
        return ResponseEntity.ok("Post descurtido com sucesso");
    }

    public ResponseEntity<DislikesDTO> getDislikes(Long userId, Long postId) {
        if (!postRepository.existsById(postId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        if (!userRepository.existsById(userId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        Post post = findPostById(postId);

        List<Dislike> dislikes = post.getDislikes();

        List<ListDislikeDTO> listDislikeDTO =
                dislikes.stream().map(dislike ->
                        new ListDislikeDTO(dislike.getId(),
                                dislike.getUser().getId(), dislike.getUser().getName()
                                .toString())).collect(Collectors.toList());

        Number totalDislikes = dislikes.size();

        return ResponseEntity.ok(new DislikesDTO(postId, userId, listDislikeDTO, totalDislikes));
    }

    public ResponseEntity<String> undeslikePost(Long userId, Long postId) {
        Dislike dislike = dislikeRepository.findByUserIdAndPostId(userId, postId);

        if (dislike == null) {
            logger.warn("Descurtida não encontrada para usuário: {} e post: {}", userId, postId);
            throw new RuntimeException("Descurtida não encontrada");
        }

        dislikeRepository.delete(dislike);
        logger.info("Descurtida removida com sucesso: {} por usuário: {}", postId, userId);
        return ResponseEntity.ok("Descurtida removida com sucesso");
    }
}
