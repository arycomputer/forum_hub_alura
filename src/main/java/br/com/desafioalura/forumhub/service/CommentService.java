package br.com.desafioalura.forumhub.service;

import br.com.desafioalura.forumhub.dto.CommentDTO;
import br.com.desafioalura.forumhub.exception.ResourceNotFoundException;
import br.com.desafioalura.forumhub.exception.UnauthorizedActionException;
import br.com.desafioalura.forumhub.model.Comment;
import br.com.desafioalura.forumhub.model.Post;
import br.com.desafioalura.forumhub.model.User;
import br.com.desafioalura.forumhub.repository.CommentRepository;
import br.com.desafioalura.forumhub.repository.PostRepository;
import br.com.desafioalura.forumhub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true) // Operação de leitura
    public List<CommentDTO> getCommentsByPostId(Long postId) {
        logger.info("Buscando comentários para o post ID: {}", postId);
        if (!postRepository.existsById(postId)) {
            logger.warn("Tentativa de buscar comentários para post ID: {} que não existe.", postId);
            throw new ResourceNotFoundException("Post não encontrado com ID: " + postId);
        }
        List<CommentDTO> comments = commentRepository.findByPostId(postId).stream()
                .map(CommentDTO::fromEntity)
                .collect(Collectors.toList());
        logger.info("Encontrados {} comentários para o post ID: {}", comments.size(), postId);
        return comments;
    }

    @Transactional // Operação de escrita
    public CommentDTO addComment(Long postId, Long userId, CommentDTO request) {
        logger.info("Tentativa de adicionar comentário ao post ID: {} pelo usuário ID: {}", postId, userId);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    logger.warn("Falha ao adicionar comentário: Post ID: {} não encontrado.", postId);
                    return new ResourceNotFoundException("Post não encontrado com ID: " + postId);
                });
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Falha ao adicionar comentário: Usuário ID: {} não encontrado.", userId);
                    return new ResourceNotFoundException("Usuário não encontrado com ID: " + userId);
                });

        Comment comment = new Comment();
        comment.setContent(request.getContent());
        comment.setPost(post);
        comment.setUser(user);
        comment.setCreatedAt(LocalDateTime.now());

        Comment saved = commentRepository.save(comment);
        logger.info("Comentário adicionado com sucesso ao post ID: {} pelo usuário ID: {}. Comentário ID: {}", postId, userId, saved.getId());
        return CommentDTO.fromEntity(saved);
    }

    @Transactional
    public CommentDTO updateComment(Long postId, Long commentId, Long userId, CommentDTO request) {
        logger.info("Tentativa de atualizar comentário ID: {} do post ID: {} pelo usuário ID: {}", commentId, postId, userId);

        Comment existingComment = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> {
                    logger.warn("Falha ao atualizar comentário: Comentário ID: {} do post ID: {} não encontrado.", commentId, postId);
                    return new ResourceNotFoundException("Comentário não encontrado com ID: " + commentId + " no post ID: " + postId);
                });

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Usuário ID: {} autenticado não encontrado ao tentar atualizar comentário ID: {}.", userId, commentId);
                    return new ResourceNotFoundException("Usuário autenticado não encontrado.");
                });

        boolean isAuthor = existingComment.getUser().getId().equals(userId);
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN; // Assumindo que User.Role é um enum

        if (!isAuthor && !isAdmin) {
            logger.warn("Usuário ID: {} não tem permissão para atualizar o comentário ID: {}.", userId, commentId);
            throw new UnauthorizedActionException("Usuário não tem permissão para atualizar este comentário.");
        }

        existingComment.setContent(request.getContent());
        existingComment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(existingComment);
        logger.info("Comentário ID: {} atualizado com sucesso do post ID: {} pelo usuário ID: {}.", commentId, postId, userId);
        return CommentDTO.fromEntity(updatedComment);
    }

    @Transactional
    public void deleteComment(Long postId, Long commentId, Long userId) {
        logger.info("Tentativa de deletar comentário ID: {} do post ID: {} pelo usuário ID: {}", commentId, postId, userId);

        Comment comment = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> {
                    logger.warn("Falha ao deletar comentário: Comentário ID: {} do post ID: {} não encontrado.", commentId, postId);
                    return new ResourceNotFoundException("Comentário não encontrado com ID: " + commentId + " no post ID: " + postId);
                });

        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("Usuário ID: {} autenticado não encontrado ao tentar deletar comentário ID: {}.", userId, commentId);
                    return new ResourceNotFoundException("Usuário autenticado não encontrado.");
                });

        boolean isAuthor = comment.getUser().getId().equals(userId);
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;

        if (!isAuthor && !isAdmin) {
            logger.warn("Usuário ID: {} não tem permissão para excluir o comentário ID: {}.", userId, commentId);
            throw new UnauthorizedActionException("Usuário não tem permissão para excluir este comentário.");
        }

        commentRepository.delete(comment);
        logger.info("Comentário ID: {} deletado com sucesso do post ID: {} pelo usuário ID: {}.", commentId, postId, userId);
    }
}

