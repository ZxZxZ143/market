package org.example.session.controllers.inventory;

import org.example.session.data.dtos.request.InventoryDto;
import org.example.session.data.dtos.response.InventoryResDto;
import org.example.session.data.mappers.InventoryMapper;
import org.example.session.security.user.UserServiceImpl;
import org.example.session.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/api/seller/inventory")
public class InventorySellerController {

    private final InventoryService inventoryService;
    private final InventoryMapper inventoryMapper;

    public InventorySellerController(InventoryService inventoryService, InventoryMapper inventoryMapper) {
        this.inventoryService = inventoryService;
        this.inventoryMapper = inventoryMapper;
    }

    @PutMapping("/products/{productId}")
    public ResponseEntity<InventoryResDto> setQuantity(
            @AuthenticationPrincipal UserServiceImpl principal,
            @PathVariable Integer productId,
            @RequestBody InventoryDto dto
    ) throws AccessDeniedException {
        var updated = inventoryService.setQuantityBySeller(principal, productId, dto);
        return ResponseEntity.ok(inventoryMapper.toResDto(updated));
    }
}
