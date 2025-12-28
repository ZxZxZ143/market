package org.example.session.mappers;

import org.example.session.data.dtos.response.CategoryResDto;
import org.example.session.data.mappers.CategoryMapper;
import org.example.session.db.entity.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

@SpringBootTest
public class CategoryMapperTest {

    @Autowired
    private CategoryMapper mapper;

    @Test
    void toResDto_shouldMapParentFields() {
        Category parent = new Category();
        parent.setId(1);
        parent.setName("Parent");
        parent.setCreatedAt(Instant.now());

        Category child = new Category();
        child.setId(2);
        child.setName("Child");
        child.setParent(parent);
        child.setCreatedAt(Instant.now());

        CategoryResDto dto = mapper.toResDto(child);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(2, dto.getId());
        Assertions.assertEquals("Child", dto.getName());
        Assertions.assertEquals(1, dto.getParentId());
        Assertions.assertEquals("Parent", dto.getParentName());
    }

    @Test
    void toResDto_shouldHandleNullParent() {
        Category category = new Category();
        category.setId(10);
        category.setName("Root");
        category.setCreatedAt(Instant.now());

        CategoryResDto dto = mapper.toResDto(category);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(10, dto.getId());
        Assertions.assertEquals("Root", dto.getName());
        Assertions.assertNull(dto.getParentId());
        Assertions.assertNull(dto.getParentName());
    }
}
