package com.fourj.kltn_be.service;

import com.fourj.kltn_be.dto.BrandDTO;
import com.fourj.kltn_be.entity.Brand;
import com.fourj.kltn_be.repository.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {
    private final BrandRepository brandRepository;

    public List<BrandDTO> getAllBrands() {
        return brandRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<BrandDTO> getBrandById(Long id) {
        return brandRepository.findById(id)
                .map(this::convertToDTO);
    }

    @Transactional
    public BrandDTO createBrand(BrandDTO brandDTO) {
        Brand brand = new Brand();
        brand.setName(brandDTO.getName());
        brand.setLogo(brandDTO.getLogo());
        Brand saved = brandRepository.save(brand);
        return convertToDTO(saved);
    }

    @Transactional
    public Optional<BrandDTO> updateBrand(Long id, BrandDTO brandDTO) {
        return brandRepository.findById(id)
                .map(brand -> {
                    brand.setName(brandDTO.getName());
                    brand.setLogo(brandDTO.getLogo());
                    Brand updated = brandRepository.save(brand);
                    return convertToDTO(updated);
                });
    }

    @Transactional
    public void deleteBrand(Long id) {
        brandRepository.deleteById(id);
    }

    private BrandDTO convertToDTO(Brand brand) {
        BrandDTO dto = new BrandDTO();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setLogo(brand.getLogo());
        return dto;
    }
}

