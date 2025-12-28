package org.example.session.controllers.product;

import org.example.session.data.dtos.request.product.ProductDto;
import org.example.session.data.dtos.request.product.ProductUpdateDto;
import org.example.session.data.dtos.response.product.ProductResDto;
import org.example.session.data.mappers.ProductMapper;
import org.example.session.security.user.UserServiceImpl;
import org.example.session.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/seller/products")
public class ProductSellerController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @Autowired
    public ProductSellerController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @PostMapping
    public ResponseEntity<ProductResDto> create(
            @AuthenticationPrincipal UserServiceImpl principal,
            @RequestBody ProductDto dto
    ) {
        var created = productService.create(principal, dto);
        var res = productMapper.toResDto(created);

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping
    public ResponseEntity<Page<ProductResDto>> myProducts(
            @AuthenticationPrincipal UserServiceImpl principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<ProductResDto> res = productService.listSellerProducts(principal, page, size).map(productMapper::toResDto);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResDto> update(
            @AuthenticationPrincipal UserServiceImpl principal,
            @PathVariable Integer id,
            @RequestBody ProductUpdateDto dto
    ) throws AccessDeniedException {
        var updated = productService.updateBySeller(principal, id, dto);
        return ResponseEntity.ok(productMapper.toResDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(
            @AuthenticationPrincipal UserServiceImpl principal,
            @PathVariable Integer id
    ) throws AccessDeniedException {
        productService.archiveBySeller(principal, id);
        return ResponseEntity.noContent().build();
    }
}
