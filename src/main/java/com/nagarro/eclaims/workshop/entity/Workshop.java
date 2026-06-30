package com.nagarro.eclaims.workshop.entity;

import com.nagarro.eclaims.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "workshops", indexes = {
    @Index(name = "idx_workshops_zip_code", columnList = "zip_code"),
    @Index(name = "idx_workshops_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Workshop extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "partner_code", nullable = false, unique = true, length = 64)
    private String partnerCode;

    @Column(name = "address_line1", nullable = false, length = 255)
    private String addressLine1;

    @Column(name = "city", nullable = false, length = 128)
    private String city;

    @Column(name = "state", nullable = false, length = 128)
    private String state;

    @Column(name = "zip_code", nullable = false, length = 32)
    private String zipCode;

    @Column(name = "phone", length = 32)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;
}

