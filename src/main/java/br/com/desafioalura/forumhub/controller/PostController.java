package br.com.desafioalura.forumhub.controller;

import br.com.desafioalura.forumhub.dto.*;
import br.com.desafioalura.forumhub.exception.ResourceNotFoundException;
import br.com.desafioalura.forumhub.exception.UnauthorizedActionException;
import br.com.desafioalura.forumhub.model.Post;
import br.com.desafioalura.forumhub.model.User;
import br.com.desafioalura.forumhub.service.AuthService;
import br.com.desafioalura.forumhub.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.util.List;

@Tag(name = "Controle de post", description = "Endpoints para registro e gerenciamento de posts")
@RestController
@RequestMapping("/forum/posts")
public class PostController {

    private final PostService postService;

    private final AuthService authService; // Service to get current authenticated user

    private final ModelMapper modelMapper; // Used for mapping between DTOs and Entities


    public PostController(PostService postService, AuthService authService, ModelMapper modelMapper) {
        this.postService = postService;
        this.authService = authService;
        this.modelMapper = modelMapper;
    }

    private User getCurrentUser() {
        return authService.getCurrentAuthenticatedUser();
    }

    @Operation(summary = "Listar todos os posts")
    @ApiResponse(responseCode = "200", description = "Posts retornados com sucesso")
    @GetMapping
    public ResponseEntity<List<PostDTO>> getAllPost() {
        List<PostDTO> posts = postService.getAllPost();
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Buscar post por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post encontrado"),
            @ApiResponse(responseCode = "404", description = "Post não encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PostDTO> getPostById(@PathVariable Long id) {
        try {
            PostDTO post = postService.getPostByIdWithDetails(id);
            return ResponseEntity.ok(post);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Criar novo post")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post criado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    @PostMapping
    public ResponseEntity<PostDTO> createPost(
            @RequestBody @Valid PostRequestDTO postRequestDTO,
            UriComponentsBuilder uriBuilder) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        PostDTO createdPost = postService.createPost(postRequestDTO, currentUser.getId());
        URI location = uriBuilder.path("/posts/{id}")
                .buildAndExpand(createdPost.getId())
                .toUri();
        return ResponseEntity.created(location).body(createdPost);
    }

    @Operation(summary = "Atualizar um post existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post atualizado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado")
    })
    @PutMapping("/{id}")
    public ResponseEntity<PostDTO> updatePost(
            @PathVariable Long id,
            @RequestBody @Valid PostUpdateDTO dto) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(postService.updatePost(id, dto, currentUser.getId()));
    }

    @Operation(summary = "Excluir (inativar) um post")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Post excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Usuário não autenticado"),
            @ApiResponse(responseCode = "403", description = "Ação não permitida"),
            @ApiResponse(responseCode = "404", description = "Post não encontrado")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        User currentUser = authService.getCurrentAuthenticatedUser();
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            postService.deletePost(id, currentUser.getId());
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (UnauthorizedActionException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @Operation(summary = "Listar posts com detalhes completos (usuário, curso, comentários, likes)")
    @ApiResponse(responseCode = "200", description = "Posts com detalhes retornados com sucesso")
    @GetMapping("/details")
    public ResponseEntity<List<PostDTO>> getAllWithDetails() {
        return ResponseEntity.ok(postService.getAllWithDetails());
    }

    @Operation(summary = "Buscar post por ID com detalhes")
    @ApiResponse(responseCode = "200", description = "Post com detalhes retornado com sucesso")
    @GetMapping("/{id}/details")
    public ResponseEntity<PostDTO> getByIdWithDetails(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getByIdWithDetails(id));
    }

    @Operation(summary = "Obter posts ativos", description = "Retorna uma lista paginada de posts ativos, ordenada pela data de criação por padrão (decrescente). Se os parâmetros de paginação não forem fornecidos, todos os posts ativos serão retornados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Posts recuperados com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(anyOf = {
                                    Page.class,
                                    List.class
                            }),
                            examples = {
                                    @ExampleObject(name = "Exemplo com Paginação e Ordenação Padrão (Decrescente)",
                                            value = "{\"content\": [{\"id\": 1, \"title\": \"Post Mais Novo\", \"createdDate\": \"2025-07-27T10:00:00Z\", \"active\": true}, {\"id\": 2, \"title\": \"Post Antigo\", \"createdDate\": \"2025-07-26T10:00:00Z\", \"active\": true}], \"pageable\": {\"pageNumber\": 0, \"pageSize\": 10, \"sort\": {\"sorted\": true, \"unsorted\": false, \"empty\": false}, \"offset\": 0, \"paged\": true, \"unpaged\": false}, \"last\": true, \"totalPages\": 1, \"totalElements\": 2, \"size\": 10, \"number\": 0, \"sort\": {\"sorted\": true, \"unsorted\": false, \"empty\": false}, \"first\": true, \"numberOfElements\": 2, \"empty\": false}"),
                                    @ExampleObject(name = "Exemplo sem Paginação",
                                            value = "[{\"id\": 1, \"title\": \"Post Mais Novo\", \"createdDate\": \"2025-07-27T10:00:00Z\", \"active\": true}, {\"id\": 2, \"title\": \"Post Antigo\", \"createdDate\": \"2025-07-26T10:00:00Z\", \"active\": true}]")
                            }))
    })
    @GetMapping("/active")
    public ResponseEntity<?> getActivePosts(
            @PageableDefault(size = 10, sort = {"createdAt"}, direction = Sort.Direction.DESC)
            Pageable pageable) {
            if (pageable.isUnpaged()) {
                List<Post> posts = postService.findAllByActiveTrueOrderByCreatedDateDesc();
                return ResponseEntity.ok(posts);
            } else {
                        if (pageable.getSort().isUnsorted()) {
                    pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("createdAt").descending());
                }
                List<PostDTO> postsPage = postService.findAllByActiveTrue(pageable);
                return ResponseEntity.ok(postsPage);
            }
    }

    @Operation(summary = "Buscar posts por título e/ou conteúdo (parcial)")
    @ApiResponse(responseCode = "200", description = "Posts encontrados com sucesso")
    @GetMapping("/search")
    public ResponseEntity<Page<PostDTO>> searchPosts(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<PostDTO> result = postService.searchPostsByTitleOrContent(title, content, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Listar posts por usuário")
    @ApiResponse(responseCode = "200", description = "Posts do usuário retornados")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostDTO>> getByUser(@PathVariable Long userId, Pageable pageable) {
        return ResponseEntity.ok(postService.getPostsByUser(userId, pageable));
    }

    @Operation(summary = "Listar posts por curso")
    @ApiResponse(responseCode = "200", description = "Posts do curso retornados")
    @GetMapping("/course/{courseId}")
    public ResponseEntity<Page<PostDTO>> getByCourse(@PathVariable Long courseId, Pageable pageable) {
        return ResponseEntity.ok(postService.getPostsByCourse(courseId, pageable));
    }
}
