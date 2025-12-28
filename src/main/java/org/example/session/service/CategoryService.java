package org.example.session.service;

import jakarta.transaction.Transactional;
import org.example.session.data.dtos.request.CategoryDto;
import org.example.session.db.entity.Category;
import org.example.session.db.repositoty.CategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepo categoryRepository;

    @Autowired
    public CategoryService(CategoryRepo categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Category create(CategoryDto dto) {
        if (dto.getName() == null || dto.getName().isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }

        Category parent = null;

        if (dto.getParentId() != null) {
            parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent category not found"));
        }

        boolean exists = (parent == null)
                ? categoryRepository.existsByNameIgnoreCaseAndParentIsNull(dto.getName())
                : categoryRepository.existsByNameIgnoreCaseAndParent_Id(dto.getName(), parent.getId());

        if (exists) {
            throw new IllegalArgumentException("Category already exists on this level");
        }

        Category c = new Category();
        c.setName(dto.getName().trim());
        c.setParent(parent);
        c.setCreatedAt(Instant.now());

        return categoryRepository.save(c);
    }

    @Transactional
    public Category get(Integer id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
    }

    @Transactional
    public List<Category> rootCategories() {
        return categoryRepository.findAllByParentIsNullOrderByNameAsc();
    }

    @Transactional
    public List<Category> children(Integer parentId) {
        return categoryRepository.findAllByParent_Id(parentId);
    }
}
