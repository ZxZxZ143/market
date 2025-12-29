package org.example.session.data.dtos.response.product;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ProductResDto {
    private Long id;

    private Long sellerId;
    private String sellerEmail;

    private Long categoryId;
    private String categoryName;

    private String title;
    private String description;

    private Long price;

    private String status;
}
