package com.example.forum.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CursorResponse<T> {

    private List<T> data;

    private String nextCursor;
    private boolean hasNext;
}
