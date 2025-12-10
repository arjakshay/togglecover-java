package com.togglecover.common.models;

import lombok.Data;

@Data
public class PaginationRequest {
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "id";
    private String sortDirection = "asc";
}