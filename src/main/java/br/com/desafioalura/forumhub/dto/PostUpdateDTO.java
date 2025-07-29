package br.com.desafioalura.forumhub.dto;

public record PostUpdateDTO(
        String title,
        String content,
        Boolean active,
        Long courseId
) {}
