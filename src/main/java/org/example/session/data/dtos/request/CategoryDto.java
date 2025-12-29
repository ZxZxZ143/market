package org.example.session.data.dtos.request;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class CategoryDto {
    private String name;
    private Integer parentId;
}
