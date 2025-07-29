package br.com.desafioalura.forumhub.dto;

import br.com.desafioalura.forumhub.model.Comment;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CommentDTO {
    private Long id;
    private String content;
    private String userEmail;
    @CreatedDate
    private LocalDateTime createdAt;
   // private String userName;

    public static CommentDTO fromEntity(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setUserEmail(comment.getUser().getEmail());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}
