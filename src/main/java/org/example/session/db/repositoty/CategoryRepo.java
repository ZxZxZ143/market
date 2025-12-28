package org.example.session.db.repositoty;

import org.example.session.db.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepo extends JpaRepository<Category, Integer> {
    List<Category> findAllByParentIsNullOrderByNameAsc();

    List<Category> findAllByParent_Id(Integer parentId);

    boolean existsByNameIgnoreCaseAndParent_Id(String name, Integer parentId);

    boolean existsByNameIgnoreCaseAndParentIsNull(String name);
}
