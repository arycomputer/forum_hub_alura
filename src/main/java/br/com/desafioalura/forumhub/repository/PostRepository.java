package br.com.desafioalura.forumhub.repository;

import br.com.desafioalura.forumhub.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.user")
    List<Post> findAllWithUser();

    @Query("SELECT p FROM Post p JOIN FETCH p.user u JOIN FETCH p.course c LEFT JOIN FETCH p.comments co LEFT JOIN FETCH p.likes l")
    List<Post> findAllWithDetails();

    @Query("SELECT p FROM Post p JOIN FETCH p.user u JOIN FETCH p.course c LEFT JOIN FETCH p.comments co LEFT JOIN FETCH p.likes l WHERE p.id = :id")
    Optional<Post> findByIdWithDetails(Long id);

    List<Post> findAll();

    Optional<Post> findByIdAndActiveTrue(Long id);

    boolean existsByTitleAndActiveTrue(String title);
    Optional<Post> findByTitleAndActiveTrue(String title);
    Page<Post> findByUserIdAndActiveTrue(Long userId, Pageable pageable);
    Page<Post> findByCourseIdAndActiveTrue(Long courseId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:title% AND p.active = true")
    Optional<Post> findByTitleContainingAndActiveTrue(@Param("title") String title);

    @Query("SELECT p FROM Post p WHERE p.content LIKE %:content% AND p.active = true")
    Optional<Post> findByContentContainingAndActiveTrue(@Param("content") String content);


    Page<Post> findByTitleContainingIgnoreCaseAndActiveTrue(String title, Pageable pageable);

    Page<Post> findByContentContainingIgnoreCaseAndActiveTrue(String content, Pageable pageable);

    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndActiveTrue(String title, String content, Pageable pageable);

    Optional<Object> findByContentAndActiveTrue(String content);

    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN FETCH p.user u " +
            "JOIN FETCH p.course c " +
            "LEFT JOIN FETCH p.comments co " +
            "LEFT JOIN FETCH p.likes l " +
            "WHERE p.active = true")
    Page<Post> findAllByActiveTrue(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.active = true ORDER BY p.createdAt DESC")
    List<Post> findAllByActiveTrue();

    Page<Post> findAllByActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    List<Post> findAllByActiveTrueOrderByCreatedAtDesc();
}