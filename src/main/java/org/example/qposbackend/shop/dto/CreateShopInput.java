package org.example.qposbackend.shop.dto;

import lombok.Data;

@Data
public class CreateShopInput {
    private String name;
    private String phone;
    private String email;
    private String address;
    private String location;
    private Boolean active;
    private String currency;
}