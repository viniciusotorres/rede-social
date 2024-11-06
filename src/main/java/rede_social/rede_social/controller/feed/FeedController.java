package rede_social.rede_social.controller.feed;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rede_social.rede_social.dto.feed.*;
import rede_social.rede_social.service.feed.FeedService;
import rede_social.rede_social.exception.feedException.PostNotFoundException;
import rede_social.rede_social.exception.feedException.CommentNotFoundException;
import rede_social.rede_social.exception.feedException.InvalidActionException;

/**
 * Controlador responsável pelas operações de Feed, incluindo posts, likes, deslikes, comentários e mais.
 */
@RestController
@RequestMapping("/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    // -------------------- Endpoints de Posts --------------------

    /**
     * Endpoint para obter os posts mais recentes de um usuário.
     *
     * @param userId ID do usuário para o qual os posts serão recuperados.
     * @return A resposta contendo os posts recentes.
     */
    @GetMapping("/{userId}")
    public ResponseEntity<FeedDTO> getRecentPosts(@PathVariable Long userId) {
        return feedService.getRecentPosts(userId);
    }

    /**
     * Endpoint para criar um novo post.
     *
     * @param userId ID do usuário que está criando o post.
     * @param content Conteúdo do post.
     * @return A resposta indicando o sucesso ou falha da operação.
     */
    @PostMapping("/{userId}/post")
    public ResponseEntity<String> createPost(@PathVariable Long userId, @RequestBody PostContentDTO content) {
        return feedService.createPost(userId, content.content());
    }

    /**
     * Endpoint para excluir um post.
     *
     * @param userId ID do usuário que está excluindo o post.
     * @param postId ID do post a ser excluído.
     * @return A resposta indicando o sucesso ou falha da operação.
     * @throws PostNotFoundException Se o post não for encontrado.
     */
    @DeleteMapping("/{userId}/post/{postId}")
    public ResponseEntity<Void> deletePost(@PathVariable Long userId, @PathVariable Long postId) throws PostNotFoundException {
        feedService.deletePost(userId, postId);
        return ResponseEntity.noContent().build();
    }

    // -------------------- Endpoints de Likes --------------------

    /**
     * Endpoint para curtir um post.
     *
     * @param userId ID do usuário que está curtindo o post.
     * @param postId ID do post a ser curtido.
     * @return A resposta indicando o sucesso ou falha da operação.
     * @throws InvalidActionException Se o post já foi curtido.
     */
    @PostMapping("/{userId}/post/{postId}/like")
    public ResponseEntity<String> likePost(@PathVariable Long userId, @PathVariable Long postId) throws InvalidActionException {
        return feedService.likePost(userId, postId);
    }

    /**
     * Endpoint para obter os likes de um post.
     *
     * @param userId ID do usuário para verificar os likes.
     * @param postId ID do post cujos likes serão recuperados.
     * @return A resposta contendo os dados dos likes.
     */
    @GetMapping("/{userId}/post/{postId}/likes")
    public ResponseEntity<LikesDTO> getLikes(@PathVariable Long userId, @PathVariable Long postId) {
        return feedService.getLikes(userId, postId);
    }

    /**
     * Endpoint para desfazer um like em um post.
     *
     * @param userId ID do usuário que está desfazendo o like.
     * @param postId ID do post no qual o like será desfeito.
     * @return A resposta indicando o sucesso ou falha da operação.
     * @throws InvalidActionException Se o post não foi curtido ou já foi desfeito.
     */
    @DeleteMapping("/{userId}/post/{postId}/like")
    public ResponseEntity<Void> unlikePost(@PathVariable Long userId, @PathVariable Long postId) throws Throwable {
        feedService.unlikePost(userId, postId);
        return ResponseEntity.noContent().build();
    }

    // -------------------- Endpoints de Deslikes --------------------

    /**
     * Endpoint para descurtir um post.
     *
     * @param userId ID do usuário que está descurtindo o post.
     * @param postId ID do post a ser descurtido.
     * @return A resposta indicando o sucesso ou falha da operação.
     * @throws InvalidActionException Se o post já foi descurtido.
     */
    @PostMapping("/{userId}/post/{postId}/deslike")
    public ResponseEntity<String> deslikePost(@PathVariable Long userId, @PathVariable Long postId) throws InvalidActionException {
        return feedService.deslikePost(userId, postId);
    }

    /**
     * Endpoint para obter os deslikes de um post.
     *
     * @param userId ID do usuário para verificar os deslikes.
     * @param postId ID do post cujos deslikes serão recuperados.
     * @return A resposta contendo os dados dos deslikes.
     */
    @GetMapping("/{userId}/post/{postId}/deslikes")
    public ResponseEntity<DislikesDTO> getDislikes(@PathVariable Long userId, @PathVariable Long postId) {
        return feedService.getDislikes(userId, postId);
    }

    /**
     * Endpoint para desfazer um deslike em um post.
     *
     * @param userId ID do usuário que está desfazendo o deslike.
     * @param postId ID do post no qual o deslike será desfeito.
     * @return A resposta indicando o sucesso ou falha da operação.
     * @throws InvalidActionException Se o post não foi descurtido ou já foi desfeito.
     */
    @DeleteMapping("/{userId}/post/{postId}/deslike")
    public ResponseEntity<Void> undeslikePost(@PathVariable Long userId, @PathVariable Long postId) throws InvalidActionException {
        feedService.undeslikePost(userId, postId);
        return ResponseEntity.noContent().build();
    }

    // -------------------- Endpoints de Comentários --------------------

    /**
     * Endpoint para comentar em um post.
     *
     * @param userId ID do usuário que está comentando no post.
     * @param postId ID do post no qual o comentário será feito.
     * @param content Conteúdo do comentário.
     * @return A resposta indicando o sucesso ou falha da operação.
     */
    @PostMapping("/{userId}/post/{postId}/comment")
    public ResponseEntity<String> commentOnPost(@PathVariable Long userId, @PathVariable Long postId, @RequestBody CommentContentDTO content) {
        return feedService.commentOnPost(userId, postId, content.content());
    }

    /**
     * Endpoint para obter os comentários de um post.
     *
     * @param userId ID do usuário para verificar os comentários.
     * @param postId ID do post cujos comentários serão recuperados.
     * @return A resposta contendo os dados dos comentários.
     */
    @GetMapping("/{userId}/post/{postId}/comments")
    public ResponseEntity<CommentsDTO> getComments(@PathVariable Long userId, @PathVariable Long postId) {
        return feedService.getComments(userId, postId);
    }

    /**
     * Endpoint para excluir um comentário de um post.
     *
     * @param userId ID do usuário que está excluindo o comentário.
     * @param postId ID do post onde o comentário será excluído.
     * @param commentId ID do comentário a ser excluído.
     * @return A resposta indicando o sucesso ou falha da operação.
     * @throws CommentNotFoundException Se o comentário não for encontrado.
     */
    @DeleteMapping("/{userId}/post/{postId}/comment/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long userId, @PathVariable Long postId, @PathVariable Long commentId) throws CommentNotFoundException {
        feedService.deleteComment(userId, postId, commentId);
        return ResponseEntity.noContent().build();
    }

    // -------------------- Endpoints de Posts Famosos --------------------

    /**
     * Endpoint para obter os posts mais famosos.
     *
     * @return A resposta contendo os posts mais famosos.
     */
    @GetMapping("/top-posts")
    public ResponseEntity<FeedTopDTO> getTopPosts() {
        return feedService.getTopFamousPosts();
    }
}
