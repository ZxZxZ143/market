package org.example.session.data.mappers;

import org.example.session.data.dtos.response.CategoryResDto;
import org.example.session.db.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "parentName", source = "parent.name")
    CategoryResDto toResDto(Category entity);
}
