package org.example.session.data.dtos.request;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDto {
    private String name;
    private Integer parentId;
}
