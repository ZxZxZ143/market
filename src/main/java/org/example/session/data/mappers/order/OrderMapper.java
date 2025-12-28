package org.example.session.data.mappers.order;

import org.example.session.data.dtos.response.order.OrderResDto;
import org.example.session.db.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = OrderItemMapper.class)
public interface OrderMapper {

    @Mapping(target = "buyerId", source = "buyer.id")
    OrderResDto toResDto(Order order);
}
