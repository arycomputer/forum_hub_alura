package br.com.desafioalura.forumhub.controller;

import br.com.desafioalura.forumhub.dto.CourseDTO;
import br.com.desafioalura.forumhub.model.Course;
import br.com.desafioalura.forumhub.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Tag(name = "Controle de cursos", description = "Endpoints para registro e gerenciamento de cursos")
@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @Operation(summary = "Listar cursos", description = "Retorna todos os cursos disponíveis")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cursos listados com sucesso",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = CourseDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<CourseDTO> courses = courseService.getAllCourses();
        return ResponseEntity.ok(courses);
    }

    @Operation(summary = "Criar curso", description = "Cria um novo curso (apenas administradores)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Curso criado com sucesso"),
            @ApiResponse(responseCode = "403", description = "Acesso negado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<String> createCourse(@RequestBody @Valid CourseDTO CourseDTO) {
        courseService.createCourse(CourseDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("Curso adicionado com sucesso!");
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long id,
            @RequestBody @Valid CourseDTO courseDTO) {
        Course updatedCourse = courseService.updateCourse(id, courseDTO);
        return ResponseEntity.ok(new CourseDTO(updatedCourse.getId(), updatedCourse.getName()));
    }

}
