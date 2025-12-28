package org.example.session.controllers.category;

import org.example.session.data.dtos.response.CategoryResDto;
import org.example.session.data.mappers.CategoryMapper;
import org.example.session.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryPublicController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @Autowired
    public CategoryPublicController(CategoryService categoryService, CategoryMapper categoryMapper) {
        this.categoryService = categoryService;
        this.categoryMapper = categoryMapper;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResDto>> roots() {
        var res = categoryService.rootCategories()
                .stream()
                .map(categoryMapper::toResDto)
                .toList();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<CategoryResDto>> children(@PathVariable Integer id) {
        var res = categoryService.children(id)
                .stream()
                .map(categoryMapper::toResDto)
                .toList();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResDto> get(@PathVariable Integer id) {
        return ResponseEntity.ok(categoryMapper.toResDto(categoryService.get(id)));
    }
}
