package br.com.desafioalura.forumhub.controller;

import br.com.desafioalura.forumhub.config.security.JwtTokenUtil;
import br.com.desafioalura.forumhub.dto.*;
import br.com.desafioalura.forumhub.model.User;
import br.com.desafioalura.forumhub.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticação", description = "Endpoints para login, registro e gerenciamento de tokens JWT")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final ModelMapper modelMapper;

    public AuthController(AuthService authService, JwtTokenUtil jwtTokenUtil, ModelMapper modelMapper) {
        this.authService = authService;
        this.modelMapper = modelMapper;
    }

    @Operation(
            summary = "Registrar novo usuário",
            description = "Cria um novo usuário com os dados fornecidos"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuário registrado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "409", description = "Email já registrado")
    })
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserRegisterDTO newUser) {
        User registeredUser = authService.registerNewUser(newUser);
        UserResponseDTO responseDTO = modelMapper.map(registeredUser, UserResponseDTO.class);
        return ResponseEntity.status(HttpStatus.CREATED).body("Utilizador registrado com sucesso!");
    }


    @Operation(
            summary = "Autenticar usuário",
            description = "Realiza login de um usuário existente e retorna tokens JWT de acesso e refresh."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticação realizada com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas (usuário ou senha incorretos)",
                    content = @Content(mediaType = "application/json")
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado. O usuário não tem permissão para acessar o recurso",
                    content = @Content(mediaType = "application/json")
            )
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> createAuthenticationToken(@RequestBody AuthRegisterDTO authenticationRequest) {
        AuthResponseDTO response  =  authService.authenticateAndGenerateToken(
                authenticationRequest.email(),authenticationRequest.password());
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Atualizar usuário",
            description = "Atualiza os dados de um usuário existente pelo ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserUpdateDTO.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PutMapping("/users/{id}")
    public ResponseEntity<UserUpdateDTO> updateUser(@PathVariable Long id, @RequestBody @Valid UserUpdateDTO UserUpdateDTO) {
        UserUpdateDTO updatedUser = authService.updateUser(id, UserUpdateDTO);
        return ResponseEntity.ok(updatedUser);
    }
}