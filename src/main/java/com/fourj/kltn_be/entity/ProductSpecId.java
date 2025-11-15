package com.fourj.kltn_be.entity;

import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpecId implements Serializable {
    private String productId;
    private String specKey;
}
