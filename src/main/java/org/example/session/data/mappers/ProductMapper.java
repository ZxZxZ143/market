package org.example.session.data.mappers;

import org.example.session.data.dtos.response.product.ProductResDto;
import org.example.session.db.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "sellerId", source = "seller.id")
    @Mapping(target = "sellerEmail", source = "seller.email")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    ProductResDto toResDto(Product product);
}