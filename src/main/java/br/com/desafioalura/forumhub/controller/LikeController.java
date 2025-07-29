package br.com.desafioalura.forumhub.controller;

import br.com.desafioalura.forumhub.repository.UserRepository;
import br.com.desafioalura.forumhub.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@Tag(name = "Controle de likes", description = "Endpoints para registro e gerenciamento de likes")
@RestController
@RequestMapping("/posts/{postId}/like")
public class LikeController {

    private final LikeService likeService;
    private final UserRepository userRepository;

    public LikeController(LikeService likeService, UserRepository userRepository) {
        this.likeService = likeService;
        this.userRepository = userRepository;
    }


    @Operation(
            summary = "Curtir um post",
            description = "Adiciona um like ao post especificado pelo ID do post."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Like adicionado com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida")
    })
    @PostMapping
    public ResponseEntity<String> like(@PathVariable Long postId, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        int totalLikes = likeService.likePost(postId, userId);
        return ResponseEntity.ok("Curtir adicionado. Total de likes: " + totalLikes);
    }

    @Operation(
            summary = "Remover curtida de um post",
            description = "Remove o like do post especificado pelo ID do post."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Like removido com sucesso"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida")
    })
    @DeleteMapping
    public ResponseEntity<String> unlike(@PathVariable Long postId, Principal principal) {
        Long userId = getUserIdFromPrincipal(principal);
        int totalLikes = likeService.unlikePost(postId, userId);
        return ResponseEntity.ok("Curtir removido. Total de likes: " + totalLikes);
    }

    private Long getUserIdFromPrincipal(Principal principal) {
        String email = principal.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"))
                .getId();
    }
}
