package org.example.session.services;

import org.example.session.data.dtos.request.CategoryDto;
import org.example.session.db.entity.Category;
import org.example.session.db.repositoty.CategoryRepo;
import org.example.session.service.CategoryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepo categoryRepo;

    @BeforeEach
    void setup() {
        categoryRepo.deleteAll();
    }

    @Test
    public void createRootCategory_shouldCreate() {
        CategoryDto dto = new CategoryDto();
        dto.setName("Electronics");
        dto.setParentId(null);

        Category created = categoryService.create(dto);

        Assertions.assertNotNull(created);
        Assertions.assertNotNull(created.getId());
        Assertions.assertEquals("Electronics", created.getName());
        Assertions.assertNull(created.getParent());
        Assertions.assertNotNull(created.getCreatedAt());
    }

    @Test
    public void createChildCategory_shouldCreateWithParent() {
        Category parent = new Category();
        parent.setName("Electronics");
        parent.setCreatedAt(Instant.now());
        parent = categoryRepo.save(parent);

        CategoryDto dto = new CategoryDto();
        dto.setName("Phones");
        dto.setParentId(parent.getId());

        Category created = categoryService.create(dto);

        Assertions.assertNotNull(created.getId());
        Assertions.assertEquals("Phones", created.getName());
        Assertions.assertNotNull(created.getParent());
        Assertions.assertEquals(parent.getId(), created.getParent().getId());
    }

    @Test
    public void create_shouldTrimName() {
        CategoryDto dto = new CategoryDto();
        dto.setName("   Books   ");

        Category created = categoryService.create(dto);

        Assertions.assertEquals("Books", created.getName());
    }

    @Test
    public void create_shouldThrow_whenNameNull() {
        CategoryDto dto = new CategoryDto();
        dto.setName(null);

        Assertions.assertThrows(IllegalArgumentException.class, () -> categoryService.create(dto));
    }

    @Test
    public void create_shouldThrow_whenNameBlank() {
        CategoryDto dto = new CategoryDto();
        dto.setName("   ");

        Assertions.assertThrows(IllegalArgumentException.class, () -> categoryService.create(dto));
    }

    @Test
    public void create_shouldThrow_whenParentNotFound() {
        CategoryDto dto = new CategoryDto();
        dto.setName("Phones");
        dto.setParentId(99999);

        Assertions.assertThrows(IllegalArgumentException.class, () -> categoryService.create(dto));
    }

    @Test
    public void create_shouldThrow_whenDuplicateRootCategoryExists() {
        Category existing = new Category();
        existing.setName("Electronics");
        existing.setCreatedAt(Instant.now());
        categoryRepo.save(existing);

        CategoryDto dto = new CategoryDto();
        dto.setName("electronics");
        dto.setParentId(null);

        Assertions.assertThrows(IllegalArgumentException.class, () -> categoryService.create(dto));
    }

    @Test
    public void create_shouldAllowSameName_inDifferentParents() {
        Category p1 = new Category();
        p1.setName("P1");
        p1.setCreatedAt(Instant.now());
        p1 = categoryRepo.save(p1);

        Category p2 = new Category();
        p2.setName("P2");
        p2.setCreatedAt(Instant.now());
        p2 = categoryRepo.save(p2);

        CategoryDto dto1 = new CategoryDto();
        dto1.setName("Phones");
        dto1.setParentId(p1.getId());
        Category c1 = categoryService.create(dto1);

        CategoryDto dto2 = new CategoryDto();
        dto2.setName("Phones");
        dto2.setParentId(p2.getId());
        Category c2 = categoryService.create(dto2);

        Assertions.assertNotNull(c1.getId());
        Assertions.assertNotNull(c2.getId());
        Assertions.assertNotEquals(c1.getId(), c2.getId());
        Assertions.assertEquals("Phones", c1.getName());
        Assertions.assertEquals("Phones", c2.getName());
        Assertions.assertEquals(p1.getId(), c1.getParent().getId());
        Assertions.assertEquals(p2.getId(), c2.getParent().getId());
    }

    @Test
    public void create_shouldThrow_whenDuplicateOnSameLevel_underSameParent() {
        Category parent = new Category();
        parent.setName("Electronics");
        parent.setCreatedAt(Instant.now());
        parent = categoryRepo.save(parent);

        CategoryDto dto1 = new CategoryDto();
        dto1.setName("Phones");
        dto1.setParentId(parent.getId());
        categoryService.create(dto1);

        CategoryDto dto2 = new CategoryDto();
        dto2.setName("phones");
        dto2.setParentId(parent.getId());

        Assertions.assertThrows(IllegalArgumentException.class, () -> categoryService.create(dto2));
    }

    @Test
    public void get_shouldReturnCategory() {
        Category c = new Category();
        c.setName("Books");
        c.setCreatedAt(Instant.now());
        c = categoryRepo.save(c);

        Category found = categoryService.get(c.getId());

        Assertions.assertNotNull(found);
        Assertions.assertEquals(c.getId(), found.getId());
        Assertions.assertEquals("Books", found.getName());
    }

    @Test
    public void get_shouldThrow_whenNotFound() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> categoryService.get(123456));
    }

    @Test
    public void rootCategories_shouldReturnOnlyRoots_sortedByNameAsc() {
        Category rootB = new Category();
        rootB.setName("Books");
        rootB.setCreatedAt(Instant.now());
        categoryRepo.save(rootB);

        Category rootA = new Category();
        rootA.setName("Appliances");
        rootA.setCreatedAt(Instant.now());
        categoryRepo.save(rootA);

        Category parent = new Category();
        parent.setName("Electronics");
        parent.setCreatedAt(Instant.now());
        parent = categoryRepo.save(parent);

        Category child = new Category();
        child.setName("Phones");
        child.setParent(parent);
        child.setCreatedAt(Instant.now());
        categoryRepo.save(child);

        List<Category> roots = categoryService.rootCategories();

        Assertions.assertNotNull(roots);
        Assertions.assertEquals(3, roots.size());
        Assertions.assertNull(roots.get(0).getParent());
        Assertions.assertNull(roots.get(1).getParent());
        Assertions.assertNull(roots.get(2).getParent());

        Assertions.assertEquals("Appliances", roots.get(0).getName());
        Assertions.assertEquals("Books", roots.get(1).getName());
        Assertions.assertEquals("Electronics", roots.get(2).getName());
    }

    @Test
    public void children_shouldReturnAllChildrenOfParent() {
        Category parent = new Category();
        parent.setName("Electronics");
        parent.setCreatedAt(Instant.now());
        parent = categoryRepo.save(parent);

        Category c1 = new Category();
        c1.setName("Phones");
        c1.setParent(parent);
        c1.setCreatedAt(Instant.now());
        categoryRepo.save(c1);

        Category c2 = new Category();
        c2.setName("Laptops");
        c2.setParent(parent);
        c2.setCreatedAt(Instant.now());
        categoryRepo.save(c2);

        Category otherParent = new Category();
        otherParent.setName("Books");
        otherParent.setCreatedAt(Instant.now());
        otherParent = categoryRepo.save(otherParent);

        Category otherChild = new Category();
        otherChild.setName("Comics");
        otherChild.setParent(otherParent);
        otherChild.setCreatedAt(Instant.now());
        categoryRepo.save(otherChild);

        List<Category> children = categoryService.children(parent.getId());

        Assertions.assertNotNull(children);
        Assertions.assertEquals(2, children.size());

        for (Category c : children) {
            Assertions.assertNotNull(c.getParent());
            Assertions.assertEquals(parent.getId(), c.getParent().getId());
        }
    }
}

