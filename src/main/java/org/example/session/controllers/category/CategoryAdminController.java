package org.example.session.controllers.category;


import org.example.session.data.dtos.request.CategoryDto;
import org.example.session.data.dtos.response.CategoryResDto;
import org.example.session.data.mappers.CategoryMapper;
import org.example.session.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/admin/categories")
public class CategoryAdminController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryAdminController(CategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

    @PostMapping
    public ResponseEntity<CategoryResDto> create(@RequestBody CategoryDto dto) {
        var created = categoryService.create(dto);
        var res = categoryMapper.toResDto(created);

        return ResponseEntity
                .created(URI.create("/api/categories/" + created.getId()))
                .body(res);
    }
}
