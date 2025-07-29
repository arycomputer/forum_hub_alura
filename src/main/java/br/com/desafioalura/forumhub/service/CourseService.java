package br.com.desafioalura.forumhub.service;

import br.com.desafioalura.forumhub.dto.CourseDTO;
import br.com.desafioalura.forumhub.exception.CourseAlreadyExistsException;
import br.com.desafioalura.forumhub.exception.ResourceNotFoundException;
import br.com.desafioalura.forumhub.model.Course;
import br.com.desafioalura.forumhub.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CourseService {
    private static final Logger logger = LoggerFactory.getLogger(CourseService.class);

    private final CourseRepository courseRepository;

    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public List<CourseDTO> getAllCourses() {
        logger.info("Buscando todos os cursos.");
        List<CourseDTO> courses = courseRepository.findAll().stream()
                .map(course -> new CourseDTO(course.getId(), course.getName()))
                .collect(Collectors.toList());
        logger.info("Total de {} cursos encontrados.", courses.size());
        return courses;
    }

    @Transactional
    public Course createCourse(CourseDTO courseDTO) {
        logger.info("Tentativa de criar novo curso com nome: {}", courseDTO.name());
         if (courseRepository.findByName(courseDTO.name()).isPresent()) {
             throw new CourseAlreadyExistsException("Curso com nome '" + courseDTO.name() + "' já existe.");
         }

        Course course = new Course();
        course.setName(courseDTO.name());
        Course savedCourse = courseRepository.save(course);
        logger.info("Curso criado com sucesso. ID: {}, Nome: {}", savedCourse.getId(), savedCourse.getName());
        return savedCourse;
    }

    @Transactional(readOnly = true) // Apenas leitura
    public Course getCourseById(Long courseId) {
        logger.info("Buscando curso por ID: {}", courseId);
        return courseRepository.findById(courseId)
                .orElseThrow(() -> {
                    logger.warn("Curso com ID: {} não encontrado.", courseId);
                    return new ResourceNotFoundException("Curso", "ID", courseId);
                });
    }

    @Transactional
    public Course updateCourse(Long id, CourseDTO courseDTO) {
        logger.info("Tentativa de atualizar curso ID: {} com novos dados: {}", id, courseDTO.name());

        Course existingCourse = courseRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Falha ao atualizar curso: Curso com ID: {} não encontrado.", id);
                    return new ResourceNotFoundException("Curso", "ID", id);
                });

        Optional<Course> courseWithSameName = courseRepository.findByName(courseDTO.name());
        if (courseWithSameName.isPresent() && !courseWithSameName.get().getId().equals(id)) {
            logger.warn("Falha ao atualizar curso ID: {}. Nome '{}' já está em uso por outro curso ID: {}.", id, courseDTO.name(), courseWithSameName.get().getId());
            throw new CourseAlreadyExistsException("Curso com nome '" + courseDTO.name() + "' já existe.");
        }

        existingCourse.setName(courseDTO.name());
        Course updatedCourse = courseRepository.save(existingCourse);
        logger.info("Curso ID: {} atualizado com sucesso para o nome: {}.", updatedCourse.getId(), updatedCourse.getName());
        return updatedCourse;
    }
}