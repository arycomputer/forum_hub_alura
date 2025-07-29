package br.com.desafioalura.forumhub.service;

import br.com.desafioalura.forumhub.dto.CommentDTO;
import br.com.desafioalura.forumhub.dto.PostDTO;
import br.com.desafioalura.forumhub.dto.PostRequestDTO;
import br.com.desafioalura.forumhub.dto.PostUpdateDTO;
import br.com.desafioalura.forumhub.exception.ResourceNotFoundException;
import br.com.desafioalura.forumhub.exception.UnauthorizedActionException;
import br.com.desafioalura.forumhub.model.Course;
import br.com.desafioalura.forumhub.model.Post;
import br.com.desafioalura.forumhub.model.User;
import br.com.desafioalura.forumhub.repository.CourseRepository;
import br.com.desafioalura.forumhub.repository.PostRepository;
import br.com.desafioalura.forumhub.repository.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PostService {

    private static final Logger logger = LoggerFactory.getLogger(PostService.class);

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final ModelMapper modelMapper;

    public PostService(PostRepository postRepository, UserRepository userRepository, CourseRepository courseRepository, ModelMapper modelMapper) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.courseRepository = courseRepository;
        this.modelMapper = modelMapper;
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> getActivePosts(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
        }
        logger.info("Buscando posts ativos com paginação: {}", pageable);
        Page<Post> posts = postRepository.findAllByActiveTrue(pageable);
        return posts.map(this::mapToPostDTOWithDetails);
    }

    @Transactional(readOnly = true)
    public PostDTO getPostByIdWithDetails(Long id) {
        logger.info("Buscando post por ID com detalhes: {}", id);
        Post post = postRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado com ID: " + id));
        return mapToPostDTOWithDetails(post);
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> searchPostsByTitleOrContent(String title, String content, Pageable pageable) {
        Page<Post> posts;
        logger.info("Buscando posts por título: '{}' ou conteúdo: '{}'", title, content);

        if (title != null && !title.trim().isEmpty() && content != null && !content.trim().isEmpty()) {
            posts = postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCaseAndActiveTrue(title, content, pageable);
        } else if (title != null && !title.trim().isEmpty()) {
            posts = postRepository.findByTitleContainingIgnoreCaseAndActiveTrue(title, pageable);
        } else if (content != null && !content.trim().isEmpty()) {
            posts = postRepository.findByContentContainingIgnoreCaseAndActiveTrue(content, pageable);
        } else {
            logger.warn("Tentativa de busca de post sem título ou conteúdo fornecido.");
            throw new IllegalArgumentException("Você deve fornecer pelo menos 'title' ou 'content' para a busca.");
        }
        return posts.map(this::mapToPostDTOWithDetails);
    }

    @Transactional(readOnly = true)
    public Page<PostDTO> getPostsByUser(Long userId, Pageable pageable) {
        logger.info("Buscando posts por usuário com ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuário não encontrado com ID: " + userId);
        }
        return postRepository.findByUserIdAndActiveTrue(userId, pageable)
                .map(this::mapToPostDTOWithDetails);
    }

    /**
     * Retorna uma página de posts ativos associados a um curso específico.
     *
     * @param courseId O ID do curso.
     * @param pageable Objeto Pageable para paginação.
     * @return Uma página de PostDTOs.
     * @throws ResourceNotFoundException Se o curso não for encontrado.
     */
    @Transactional(readOnly = true)
    public Page<PostDTO> getPostsByCourse(Long courseId, Pageable pageable) {
        logger.info("Buscando posts por curso com ID: {}", courseId);
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Curso não encontrado com ID: " + courseId);
        }
        return postRepository.findByCourseIdAndActiveTrue(courseId, pageable)
                .map(this::mapToPostDTOWithDetails);
    }

    @Transactional
    public PostDTO createPost(PostRequestDTO dto, Long userId) {
        logger.info("Tentativa de criar post para usuário ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + userId));

        Course course = courseRepository.findById(dto.courseId())
                .orElseThrow(() -> new ResourceNotFoundException("Curso não encontrado com ID: " + dto.courseId()));

        Post post = new Post();
        post.setTitle(dto.title());
        post.setContent(dto.content());
        post.setCourse(course);
        post.setUser(user);
        post.setCreatedAt(LocalDateTime.now());
        post.setActive(true);

        Post saved = postRepository.save(post);
        logger.info("Post criado com sucesso. ID: {}", saved.getId());
        return mapToPostDTOWithDetails(saved);
    }

    @Transactional
    public PostDTO updatePost(Long postId, PostUpdateDTO dto, Long currentUserId) {
        logger.info("Tentativa de atualizar post ID: {} pelo usuário ID: {}", postId, currentUserId);
        Post existingPost = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado com ID: " + postId));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + currentUserId));

        if (!existingPost.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != User.Role.ADMIN) {
            logger.warn("Usuário ID: {} tentou atualizar post ID: {} sem permissão.", currentUserId, postId);
            throw new UnauthorizedActionException("Você não tem permissão para editar este post.");
        }

           modelMapper.map(dto, existingPost);

        if (dto.courseId() != null && !dto.courseId().equals(existingPost.getCourse().getId())) {
            Course newCourse = courseRepository.findById(dto.courseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Novo curso não encontrado com ID: " + dto.courseId()));
            existingPost.setCourse(newCourse);
        }

        existingPost.setUpdatedAt(LocalDateTime.now());

        Post updated = postRepository.save(existingPost);
        logger.info("Post ID: {} atualizado com sucesso.", updated.getId());
        return mapToPostDTOWithDetails(updated);
    }

    @Transactional
    public void deletePost(Long id, Long currentUserId) {
        logger.info("Tentativa de deletar post ID: {} pelo usuário ID: {}", id, currentUserId);
        Post existingPost = postRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado com ID: " + id + " ou já inativo."));

        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + currentUserId));

        if (!existingPost.getUser().getId().equals(currentUser.getId()) && currentUser.getRole() != User.Role.ADMIN) {
            logger.warn("Usuário ID: {} tentou deletar post ID: {} sem permissão.", currentUserId, id);
            throw new UnauthorizedActionException("Você não tem permissão para deletar este post.");
        }

        existingPost.setActive(false);
        postRepository.save(existingPost);
        logger.info("Post ID: {} deletado (inativado) com sucesso.", id);
    }

    private PostDTO mapToPostDTOWithDetails(Post post) {
        PostDTO postDTO = modelMapper.map(post, PostDTO.class);

        postDTO.setUserEmail(post.getUser() != null ? post.getUser().getEmail() : null);
        postDTO.setLikesCount(post.getLikes() != null ? post.getLikes().size() : 0);
        postDTO.setCourseId(post.getCourse() != null ? post.getCourse().getId() : null);
        postDTO.setCourseName(post.getCourse() != null ? post.getCourse().getName() : null);

        postDTO.setComments(post.getComments() != null ?
                post.getComments().stream()
                        .map(comment -> {
                            CommentDTO commentDTO = modelMapper.map(comment, CommentDTO.class);
                            commentDTO.setUserEmail(comment.getUser() != null ? comment.getUser().getEmail() : null);
                            return commentDTO;
                        })
                        .toList() : Collections.emptyList());

        return postDTO;
    }

    public List<PostDTO> getAllPost() {
        return postRepository.findAll().stream()
                .map(post -> new PostDTO(
                        post.getId(),
                        post.getTitle(),
                        post.getContent(),
                        post.getCreatedAt(),
                        post.getUpdatedAt(),
                        post.isActive(),
                        post.getUser() != null ? post.getUser().getEmail() : null,
                        post.getLikes() != null ? post.getLikes().size() : 0,
                        post.getCourse() != null ? post.getCourse().getId() : null,
                        post.getCourse() != null ? post.getCourse().getName() : null,
                        post.getComments() != null ?
                                post.getComments().stream().map(comment -> new CommentDTO(
                                        comment.getId(),
                                        comment.getContent(),
                                        comment.getUser() != null ? comment.getUser().getEmail() : null,
                                        comment.getCreatedAt()
                                )).toList() : Collections.emptyList()
                ))
                .toList();
    }

    public List<PostDTO> getAllWithDetails() {
        return postRepository.findAllWithDetails().stream()
                .map(post -> modelMapper.map(post, PostDTO.class))
                .toList();
    }

    public PostDTO getByIdWithDetails(Long id) {
        return postRepository.findByIdWithDetails(id)
                .map(post -> modelMapper.map(post, PostDTO.class))
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado com ID: " + id));
    }

    public List<Post> findAllByActiveTrueOrderByCreatedDateDesc() {
        return postRepository.findAllByActiveTrueOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<PostDTO> findAllByActiveTrue(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdDate").descending());
        }

        Page<Post> posts = postRepository.findAllByActiveTrue(pageable);
        return posts.stream()
                .map(this::convertToPostResponse)
                .collect(Collectors.toList());
    }

    private PostDTO convertToPostResponse(Post post) {
        PostDTO pr = new PostDTO();
        pr.setId(post.getId());
        pr.setTitle(post.getTitle());
        pr.setContent(post.getContent());
        pr.setCreatedAt(post.getCreatedAt());
        pr.setUpdatedAt(post.getUpdatedAt()); // Adicione o updateAt
        pr.setActive(post.isActive());     // Adicione o active
        pr.setUserEmail(post.getUser() != null ? post.getUser().getEmail() : null); // Verifica null
        pr.setLikesCount(post.getLikes() != null ? post.getLikes().size() : 0);
        pr.setCourseId(post.getCourse() != null ? post.getCourse().getId() : null); // Verifica null
        pr.setCourseName(post.getCourse() != null ? post.getCourse().getName() : null); // Verifica null

        pr.setComments(post.getComments() != null ? post.getComments().stream().map(comment -> {
            CommentDTO cr = new CommentDTO();
            cr.setId(comment.getId());
            cr.setContent(comment.getContent());
            cr.setCreatedAt(comment.getCreatedAt());
            cr.setUserEmail(comment.getUser() != null ? comment.getUser().getEmail() : null);
            return cr;
        }).collect(Collectors.toList()) : List.of());

        return pr;
    }
}