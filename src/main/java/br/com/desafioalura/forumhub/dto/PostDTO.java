package br.com.desafioalura.forumhub.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Setter @Getter @AllArgsConstructor @NoArgsConstructor
public class PostDTO {
    private Long id;
    private String title;
    private String content;
    private @CreatedDate LocalDateTime createdAt;
    private @UpdateTimestamp LocalDateTime updatedAt;
    @Builder.Default
    private boolean active = true;
    private String userEmail;
    private int likesCount;
    private Long courseId;
    private String courseName;
    private List<CommentDTO> comments;

}
