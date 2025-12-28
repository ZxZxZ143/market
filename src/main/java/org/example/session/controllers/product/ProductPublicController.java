package org.example.session.controllers.product;

import org.example.session.data.dtos.response.product.ProductResDto;
import org.example.session.data.mappers.ProductMapper;
import org.example.session.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductPublicController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @Autowired
    public ProductPublicController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @GetMapping
    public ResponseEntity<Page<ProductResDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ProductResDto> res = productService.listPublic(page, size).map(productMapper::toResDto);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResDto> get(@PathVariable Integer id) {
        ProductResDto res = productMapper.toResDto(productService.getById(id));
        return ResponseEntity.ok(res);
    }
}