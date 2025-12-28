package org.example.session.data.mappers.cart;


import org.example.session.data.dtos.response.cart.CartResDto;
import org.example.session.db.entity.Cart;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = CartItemMapper.class)
public interface CartMapper {

    @Mapping(target = "buyerId", source = "buyer.id")
    @Mapping(target = "totalAmount", ignore = true)
    CartResDto toResDto(Cart cart);

    @AfterMapping
    default void fillTotal(Cart cart, @MappingTarget CartResDto dto) {
        long total = 0L;
        if (cart.getItems() != null) {
            for (var i : cart.getItems()) {
                if (i.getPriceSnapshot() != null && i.getQuantity() != null) {
                    total = total + (i.getPriceSnapshot() * (i.getQuantity()));
                }
            }
        }
        dto.setTotalAmount(total);
    }
}