package com.matchaworld.backend.dto.response.company;

import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PagedCompanyResponse {

    private List<CompanyResponse> content;
    private int page;
    private int size;
    private int totalPages;
    private long totalElements;
}
