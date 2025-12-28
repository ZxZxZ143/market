package org.example.session.controllers.product;

import org.example.session.data.dtos.request.product.ProductUpdateDto;
import org.example.session.data.dtos.response.product.ProductResDto;
import org.example.session.data.mappers.ProductMapper;
import org.example.session.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
public class ProductAdminController {

    private final ProductService productService;
    private final ProductMapper productMapper;

    @Autowired
    public ProductAdminController(ProductService productService, ProductMapper productMapper) {
        this.productService = productService;
        this.productMapper = productMapper;
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResDto> update(@PathVariable Integer id, @RequestBody ProductUpdateDto dto) {
        var updated = productService.updateByAdmin(id, dto);
        return ResponseEntity.ok(productMapper.toResDto(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> archive(@PathVariable Integer id) {
        productService.archiveByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
