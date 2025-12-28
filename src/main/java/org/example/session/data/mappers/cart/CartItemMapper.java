package org.example.session.data.mappers.cart;

import org.example.session.data.dtos.response.cart.CartItemResDto;
import org.example.session.db.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productTitle", source = "product.title")
    @Mapping(target = "subtotal", expression = "java(subtotal(entity))")
    CartItemResDto toResDto(CartItem entity);

    default Long subtotal(CartItem entity) {
        if (entity.getPriceSnapshot() == null || entity.getQuantity() == null) return 0L;
        return entity.getPriceSnapshot() * (Long.valueOf(entity.getQuantity()));
    }
}

