package org.example.session.controllers.inventory;

import org.example.session.data.dtos.response.InventoryResDto;
import org.example.session.data.mappers.InventoryMapper;
import org.example.session.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/inventory")
public class InventoryPublicController {

    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;

    @Autowired
    public InventoryPublicController(InventoryService inventoryService, InventoryMapper inventoryMapper) {
        this.inventoryService = inventoryService;
        this.inventoryMapper = inventoryMapper;
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<InventoryResDto> getByProduct(@PathVariable Integer productId) {
        var res = inventoryMapper.toResDto(inventoryService.getByProductId(productId));
        return ResponseEntity.ok(res);
    }
}
