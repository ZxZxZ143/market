package org.example.session.data.dtos.response;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Data
public class CategoryResDto {
    private Integer id;
    private String name;

    private Integer parentId;
    private String parentName;

    private Instant createdAt;
}
