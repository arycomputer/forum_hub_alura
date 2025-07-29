package br.com.desafioalura.forumhub.controller;

import br.com.desafioalura.forumhub.dto.CommentDTO;
import br.com.desafioalura.forumhub.repository.UserRepository;
import br.com.desafioalura.forumhub.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/posts/{postId}/comments")
@Tag(name = "Comentários", description = "Operações relacionadas aos comentários dos posts")
public class CommentController {

    private final CommentService commentService;
    private final UserRepository userRepository;

    public CommentController(CommentService commentService, UserRepository userRepository) {
        this.commentService = commentService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Listar comentários de um post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de comentários retornada com sucesso")
    })
    @GetMapping
    public ResponseEntity<List<CommentDTO>> listComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPostId(postId));
    }

    @Operation(summary = "Adicionar comentário em um post")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comentário adicionado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PostMapping
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentDTO request,
            Principal principal) {

        Long userId = getUserIdFromPrincipal(principal);
        CommentDTO createdComment = commentService.addComment(postId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComment);
    }

    @Operation(summary = "Atualizar comentário", description = "Atualiza um comentário existente. Apenas o autor do comentário ou um administrador pode atualizar.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Comentário atualizado com sucesso",
                    content = @Content(schema = @Schema(implementation = CommentDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados do comentário inválidos"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não tem permissão para atualizar o comentário"),
            @ApiResponse(responseCode = "404", description = "Comentário, post ou usuário não encontrado")
    })
    @PutMapping("/{commentId}") // Usamos PUT para atualização completa
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentDTO request, // O DTO de requisição pode ser o mesmo, ou um específico para update
            Principal principal) {

        Long userId = getUserIdFromPrincipal(principal);
        CommentDTO updatedComment = commentService.updateComment(postId, commentId, userId, request);
        return ResponseEntity.ok(updatedComment);
    }

    @Operation(summary = "Excluir comentário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Comentário removido com sucesso"),
            @ApiResponse(responseCode = "403", description = "Usuário não tem permissão para excluir o comentário"),
            @ApiResponse(responseCode = "404", description = "Comentário ou usuário não encontrado")
    })
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            Principal principal) {

        Long userId = getUserIdFromPrincipal(principal);
        commentService.deleteComment(postId, commentId, userId);
        return ResponseEntity.noContent().build();
    }

    private Long getUserIdFromPrincipal(Principal principal) {
        String email = principal.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"))
                .getId();
    }
}