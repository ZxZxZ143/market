package org.example.session.data.mappers.order;

import org.example.session.data.dtos.response.order.OrderItemResDto;
import org.example.session.db.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productTitleSnapshot", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    OrderItemResDto toResDto(OrderItem entity);
}
