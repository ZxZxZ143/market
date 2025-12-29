package org.example.session.data.dtos.request.product;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ProductDto {
    private Integer categoryId;
    private String title;
    private String description;
    private Long price;
}
