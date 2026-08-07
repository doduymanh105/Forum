package com.example.forum.feature.chat.dto.chatResponseDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomPageable<T> {
    private int currentPage;
    private int pageSize;
    private int totalPages;
    private Long totalElements;
    private List<T> data;

    public static <T> CustomPageable<T> mapToCustomPageable(Page<T> pageT){
        return CustomPageable.<T>builder()
                .currentPage(pageT.getNumber())
                .pageSize(pageT.getSize())
                .totalPages(pageT.getTotalPages())
                .totalElements(pageT.getTotalElements())
                .data(pageT.getContent())
                .build();
    }

}
