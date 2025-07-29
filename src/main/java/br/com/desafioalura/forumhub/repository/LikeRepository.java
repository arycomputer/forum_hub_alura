package br.com.desafioalura.forumhub.repository;

import br.com.desafioalura.forumhub.model.Like;
import br.com.desafioalura.forumhub.model.Post;
import br.com.desafioalura.forumhub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserAndPost(User user, Post post);
    Optional<Like> findByUserAndPost(User user, Post post);
    int countByPost(Post post);
}
