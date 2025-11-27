package com.matchaworld.backend.service.company;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.matchaworld.backend.domain.Company;
import com.matchaworld.backend.dto.response.company.CompanyResponse;
import com.matchaworld.backend.mapper.CompanyMapper;
import com.matchaworld.backend.repository.CompanyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyServiceImpl implements CompanyService {

    private final CompanyRepository companyRepository;

    @Override
    public List<CompanyResponse> getCompaniesByCategory(String categoryName) {
        List<Company> companies = companyRepository.findByCategoryName(categoryName);
        return companies.stream()
                .map(CompanyMapper::toResponse)
                .collect(Collectors.toList());
    }
}
