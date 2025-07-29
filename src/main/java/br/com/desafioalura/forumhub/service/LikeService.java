package br.com.desafioalura.forumhub.service;

import br.com.desafioalura.forumhub.exception.ResourceNotFoundException;
import br.com.desafioalura.forumhub.model.Like;
import br.com.desafioalura.forumhub.model.Post;
import br.com.desafioalura.forumhub.model.User;
import br.com.desafioalura.forumhub.repository.LikeRepository;
import br.com.desafioalura.forumhub.repository.PostRepository;
import br.com.desafioalura.forumhub.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LikeService {

    private static final Logger logger = LoggerFactory.getLogger(LikeService.class);

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public LikeService(LikeRepository likeRepository,
                       PostRepository postRepository,
                       UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public int likePost(Long postId, Long userId) {

       logger.info("Tentativa de curtir post ID: {} pelo usuário ID: {}", postId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Falha ao curtir post ID: {}. Usuário ID: {} não encontrado.", postId, userId);
                    return new ResourceNotFoundException("Usuário não encontrado com ID: " + userId);
                });

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    logger.warn("Falha ao curtir post. Post ID: {} não encontrado.", postId);
                    return new ResourceNotFoundException("Post não encontrado com ID: " + postId);
                });

        boolean alreadyLiked = likeRepository.existsByUserAndPost(user, post);
        if (alreadyLiked) {
            logger.info("Usuário ID: {} já havia curtido o post ID: {}. Nenhuma ação realizada.", userId, postId);
            throw new IllegalStateException("Você já curtiu este post.");
        }

        Like like = new Like();
        like.setUser(user);
        like.setPost(post);
        like.setCreatedAt(LocalDateTime.now());

        likeRepository.save(like);
        int likesCount = likeRepository.countByPost(post);
        logger.info("Post ID: {} curtido com sucesso pelo usuário ID: {}. Total de curtidas: {}", postId, userId, likesCount);
        return likesCount;

    }

    @Transactional
    public int unlikePost(Long postId, Long userId) {
        logger.info("Tentativa de descurtir post ID: {} pelo usuário ID: {}", postId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.warn("Falha ao descurtir post ID: {}. Usuário ID: {} não encontrado.", postId, userId);
                    return new ResourceNotFoundException("Usuário não encontrado com ID: " + userId);
                });

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> {
                    logger.warn("Falha ao descurtir post. Post ID: {} não encontrado.", postId);
                    return new ResourceNotFoundException("Post não encontrado com ID: " + postId);
                });

        Like like = likeRepository.findByUserAndPost(user, post)
                .orElseThrow(() -> {
                    logger.warn("Falha ao descurtir post ID: {}. Curtida do usuário ID: {} não encontrada.", postId, userId);
                    return new ResourceNotFoundException("Você não curtiu este post ou a curtida não foi encontrada.");
                });

        likeRepository.delete(like);
        int likesCount = likeRepository.countByPost(post);
        logger.info("Curtida do post ID: {} removida com sucesso pelo usuário ID: {}. Total de curtidas: {}", postId, userId, likesCount);
        return likesCount;
    }
}

