package br.com.desafioalura.forumhub.service;

import br.com.desafioalura.forumhub.dto.*;
import br.com.desafioalura.forumhub.exception.AuthenticationException;
import br.com.desafioalura.forumhub.exception.ResourceNotFoundException;
import br.com.desafioalura.forumhub.exception.UserAlreadyExistsException;
import br.com.desafioalura.forumhub.model.User;
import br.com.desafioalura.forumhub.repository.UserRepository;
import br.com.desafioalura.forumhub.config.security.JwtTokenUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService userDetailsService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenUtil jwtTokenUtil,
                       JwtUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public User registerNewUser(UserRegisterDTO newUserDTO) {

        logger.info("Tentativa de registrar novo usuário com e-mail: {}", newUserDTO.getEmail());
        if (userRepository.findByEmail(newUserDTO.getEmail()).isPresent()) {
            logger.warn("Falha no registro: O email '{}' já está em uso.", newUserDTO.getEmail());
            throw new UserAlreadyExistsException("O email '" + newUserDTO.getEmail() + "' já está em uso.");
        }

        User user = new User();
        user.setEmail(newUserDTO.getEmail());
        user.setPassword(passwordEncoder.encode(newUserDTO.getPassword()));
        user.setRole(User.Role.USER);
        user.setActive(true);

        User savedUser = userRepository.save(user);
        logger.info("Usuário registrado com sucesso: {}", savedUser.getEmail());
        return savedUser;
    }

     public AuthResponseDTO authenticateAndGenerateToken(String email, String password) {
         logger.info("Tentativa de autenticação para o e-mail: {}", email);
         try {
             authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
         } catch (DisabledException e) {
             logger.warn("Falha na autenticação para {}: Usuário desativado.", email);
             throw new AuthenticationException("UTILIZADOR DESATIVADO", e);
         } catch (BadCredentialsException e) {
             logger.warn("Falha na autenticação para {}: Credenciais inválidas.", email);
             throw new AuthenticationException("CREDENCIAS INVÁLIDAS", e);
         }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("Usuário com e-mail '{}' não encontrado no banco de dados após autenticação bem-sucedida. Isso indica um problema de sincronização.", email);
                    return new AuthenticationException("Erro interno: Usuário não encontrado após autenticação.");
                });

        final String token = jwtTokenUtil.generateToken(userDetails, String.valueOf(user.getRole()));
         logger.info("Autenticação bem-sucedida para o e-mail: {}", email);

         return new AuthResponseDTO(email, token, user.getRole());

    }

    @PreAuthorize("hasRole('ADMIN')")
        @Transactional
        public UserUpdateDTO updateUser(Long id, @Valid UserUpdateDTO userUpdateDTO) {
        logger.info("Tentativa de atualizar usuário com ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Falha na atualização: Usuário com ID {} não encontrado.", id);
                    return new ResourceNotFoundException("User", "ID", id);
                });

            user.setRole(userUpdateDTO.role());
            user.setActive(userUpdateDTO.active());
        User updatedUser = userRepository.save(user);
        logger.info("Usuário com ID {} atualizado com sucesso.", updatedUser.getId());

        return convertToUserDTO(updatedUser);
        }

    private UserUpdateDTO convertToUserDTO(User user) {
        return new UserUpdateDTO(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.isActive()
        ) ;
    }

      public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("Tentativa de obter usuário autenticado sem autenticação válida.");
            throw new AccessDeniedException("Nenhum usuário autenticado no contexto de segurança.");
        }

        Object principal = authentication.getPrincipal();
        String username;

        if (principal instanceof UserDetails userDetails) {
            username = userDetails.getUsername();
        } else if (principal instanceof String) {
            username = (String) principal;
        } else {
            logger.error("Tipo de principal desconhecido: {}", principal.getClass().getName());
            throw new AccessDeniedException("Não foi possível determinar o nome de usuário autenticado.");
        }

        return userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    logger.error("Usuário autenticado com e-mail '{}' não encontrado no banco de dados.", username);
                    return new ResourceNotFoundException("User", "e-mail", username);
                });
    }
}