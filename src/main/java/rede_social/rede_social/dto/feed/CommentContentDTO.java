package rede_social.rede_social.dto.feed;

/**
 * DTO para o conteúdo de um comentário em um post.
 * Representa o corpo do comentário feito em um post.
 */
public record CommentContentDTO(
        String content // O conteúdo do comentário
) {}
