package org.example.session.data.mappers;

import org.example.session.data.dtos.response.InventoryResDto;
import org.example.session.db.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "createdAt", ignore = true)
    InventoryResDto toResDto(Inventory entity);
}
