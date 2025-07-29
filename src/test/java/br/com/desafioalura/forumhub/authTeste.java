package br.com.desafioalura.forumhub;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content; // Importe content

import br.com.desafioalura.forumhub.controller.AuthController;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.desafioalura.forumhub.config.security.JwtTokenUtil;
import br.com.desafioalura.forumhub.dto.AuthRegisterDTO;
import br.com.desafioalura.forumhub.dto.AuthResponseDTO;
import br.com.desafioalura.forumhub.dto.UserRegisterDTO;
import br.com.desafioalura.forumhub.dto.UserResponseDTO;
import br.com.desafioalura.forumhub.dto.UserUpdateDTO;
import br.com.desafioalura.forumhub.exception.ResourceNotFoundException;
import br.com.desafioalura.forumhub.exception.UserAlreadyExistsException;
import br.com.desafioalura.forumhub.model.User; // Importe a classe User
import br.com.desafioalura.forumhub.model.User.Role; // Importe o enum Role se usado
import br.com.desafioalura.forumhub.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@WebMvcTest(AuthController.class) // Testa apenas AuthController
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc; // Objeto para fazer requisições HTTP simuladas

    @Autowired
    private ObjectMapper objectMapper; // Para converter objetos Java em JSON e vice-versa

    @MockitoBean // Cria um mock para a dependência AuthService
    private AuthService authService;

    @MockitoBean // Cria um mock para a dependência JwtTokenUtil (mesmo que não esteja diretamente usada nos métodos testados)
    private JwtTokenUtil jwtTokenUtil;

    @MockitoBean // Cria um mock para a dependência ModelMapper
    private ModelMapper modelMapper;

    // --- Testes para o endpoint /auth/register ---

    @Test
    @DisplayName("Deve registrar um novo usuário com sucesso e retornar status 201")
    void registerUser_Success() throws Exception {
        UserRegisterDTO registerDTO = new UserRegisterDTO("test@example.com", "Password123!",true, Role.ADMIN);

        User registeredUser = new User();
        registeredUser.setId(1L);
        registeredUser.setEmail("test@example.com");
        registeredUser.setPassword("encodedPassword");
        registeredUser.setRole(Role.USER);

        UserResponseDTO responseDTO = new UserResponseDTO(1L, "test@example.com",  Role.USER);

        when(authService.registerNewUser(any(UserRegisterDTO.class))).thenReturn(registeredUser);
        when(modelMapper.map(any(User.class), eq(UserResponseDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Utilizador registrado com sucesso!"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Não deve registrar usuário com dados inválidos e retornar status 400")
    void registerUser_InvalidInput() throws Exception {
        UserRegisterDTO registerDTO = new UserRegisterDTO("", "short",false,Role.USER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest()) // Espera status 400 Bad Request
                .andExpect(jsonPath("$.email").exists()) // Verifica se há erro para o campo email
                .andExpect(jsonPath("$.password").exists()) // Verifica se há erro para o campo password
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Não deve registrar usuário com email já existente e retornar status 409")
    void registerUser_EmailAlreadyExists() throws Exception {
        UserRegisterDTO registerDTO = new UserRegisterDTO("existing@example.com", "ValidPassword123!",false,Role.USER);

        when(authService.registerNewUser(any(UserRegisterDTO.class)))
                .thenThrow(new UserAlreadyExistsException("O email 'existing@example.com' já está em uso."));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isConflict()) // Espera status 409 Conflict
                .andExpect(jsonPath("$.message").value("O email 'existing@example.com' já está em uso.")) // Verifica a mensagem de erro
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    @DisplayName("Deve autenticar usuário com sucesso e retornar status 200 com token")
    void createAuthenticationToken_Success() throws Exception {
        AuthRegisterDTO authDTO = new AuthRegisterDTO("user@example.com", "password123");
        AuthResponseDTO authResponseDTO = new AuthResponseDTO("fake_jwt_token", "fake_refresh_token",Role.USER);

        // Comportamento esperado do mock do serviço
        when(authService.authenticateAndGenerateToken(any(String.class), any(String.class)))
                .thenReturn(authResponseDTO);

        // Realiza a requisição POST para login
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isOk()) // Espera status 200 OK
                .andExpect(jsonPath("$.jwtToken").value("fake_jwt_token")) // Verifica o token JWT na resposta
                .andExpect(jsonPath("$.refreshToken").value("fake_refresh_token")) // Verifica o refresh token
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Não deve autenticar com credenciais inválidas e retornar status 401")
    void createAuthenticationToken_InvalidCredentials() throws Exception {
        // Dados de login inválidos
        AuthRegisterDTO authDTO = new AuthRegisterDTO("invalid@example.com", "wrongpassword");

        // Mock para simular que o serviço lança BadCredentialsException
        when(authService.authenticateAndGenerateToken(any(String.class), any(String.class)))
                .thenThrow(new BadCredentialsException("Credenciais inválidas."));

        // Realiza a requisição POST e espera o status 401 Unauthorized
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isUnauthorized()) // Espera status 401 Unauthorized
                // O @ControllerAdvice para BadCredentialsException pode precisar ser configurado
                // para retornar uma mensagem específica. Aqui, verificamos apenas o status.
                .andDo(MockMvcResultHandlers.print());
    }

    // --- Testes para o endpoint /auth/users/{id} (updateUser) ---

    @Test
    @DisplayName("Deve atualizar um usuário com sucesso e retornar status 200")
    void updateUser_Success() throws Exception {
        Long userId = 1L;
        // Dados de atualização válidos
        UserUpdateDTO updateDTO = new UserUpdateDTO(2L, "NewPassword123!",Role.USER,false);
        // Mock do DTO de resposta de atualização
        UserUpdateDTO updatedResponseDTO = new UserUpdateDTO(3L, "NewPassword123!",Role.ADMIN,false); // Senha não deve ser retornada em DTO real

        // Comportamento esperado do mock do serviço
        when(authService.updateUser(eq(userId), any(UserUpdateDTO.class))).thenReturn(updatedResponseDTO);

        // Realiza a requisição PUT
        mockMvc.perform(put("/auth/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk()) // Espera status 200 OK
                .andExpect(jsonPath("$.email").value("updated.email@example.com")) // Verifica o email atualizado
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Não deve atualizar usuário não encontrado e retornar status 404")
    void updateUser_NotFound() throws Exception {
        Long userId = 99L; // ID de usuário não existente
        UserUpdateDTO updateDTO = new UserUpdateDTO(3L, "AnyPassword123!",Role.ADMIN, false);

        // Mock para simular que o serviço lança ResourceNotFoundException
        when(authService.updateUser(eq(userId), any(UserUpdateDTO.class)))
                .thenThrow(new ResourceNotFoundException("Usuário", "ID", userId));

        // Realiza a requisição PUT e espera status 404 Not Found
        mockMvc.perform(put("/auth/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound()) // Espera status 404 Not Found
                .andExpect(jsonPath("$.message").value("Usuário não encontrado com ID: 99")) // Verifica a mensagem de erro
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Não deve atualizar usuário com dados inválidos e retornar status 400")
    void updateUser_InvalidInput() throws Exception {
        Long userId = 1L;
        // Dados de atualização inválidos (email vazio, senha muito curta)
        UserUpdateDTO updateDTO = new UserUpdateDTO(1L, "short",Role.ADMIN, false);

        // Realiza a requisição PUT e espera status 400 Bad Request
        mockMvc.perform(put("/auth/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest()) // Espera status 400 Bad Request
                .andExpect(jsonPath("$.email").exists()) // Verifica se há erro para o campo email
                .andExpect(jsonPath("$.password").exists()) // Verifica se há erro para o campo password
                .andDo(MockMvcResultHandlers.print());
    }
}