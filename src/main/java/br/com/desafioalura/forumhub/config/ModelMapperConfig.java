package br.com.desafioalura.forumhub.config;

import br.com.desafioalura.forumhub.dto.CommentDTO;
import br.com.desafioalura.forumhub.dto.PostDTO;
import br.com.desafioalura.forumhub.model.Comment;
import br.com.desafioalura.forumhub.model.Post;

import org.modelmapper.Conditions;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setPropertyCondition(Conditions.isNotNull()) // para evitar sobrescritas de null
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);

        TypeMap<Post, PostDTO> typeMap = mapper.createTypeMap(Post.class, PostDTO.class);

        Converter<Set<?>, Integer> likesCountConverter = ctx -> {
            Set<?> likes = ctx.getSource();
            return (likes != null) ? likes.size() : 0;
        };


        typeMap.addMappings(map -> {
            map.map(src -> src.getUser().getEmail(), PostDTO::setUserEmail);
            map.map(src -> src.getCourse().getId(), PostDTO::setCourseId);
            map.map(src -> src.getCourse().getName(), PostDTO::setCourseName);
            map.map(src ->
                            src.getLikes() != null ? src.getLikes().size() : 0,
                    PostDTO::setLikesCount);
        });

        Converter<List<Comment>, List<CommentDTO>> commentConverter = ctx -> {
            if (ctx.getSource() == null) return Collections.emptyList();
            return ctx.getSource().stream().map(comment -> new CommentDTO(
                    comment.getId(),
                    comment.getContent(),
                    comment.getUser() != null ? comment.getUser().getEmail() : null,
                    comment.getCreatedAt()
            )).collect(Collectors.toList());
        };

        typeMap.addMappings(map -> {
            map.using(likesCountConverter).map(Post::getLikes, PostDTO::setLikesCount);
            map.using(commentConverter).map(Post::getComments, PostDTO::setComments);
        });

        return mapper;
    }
}
