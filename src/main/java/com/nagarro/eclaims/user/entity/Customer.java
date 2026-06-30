package com.nagarro.eclaims.user.entity;

import com.nagarro.eclaims.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, unique = true, length = 64)
    private String customerNumber;

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(length = 32)
    private String phone;

    @Column(length = 255)
    private String addressLine1;

    @Column(length = 128)
    private String city;

    @Column(length = 128)
    private String state;

    @Column(length = 32)
    private String zipCode;

    @Column(length = 128)
    private String country;

    @Column(nullable = false, length = 32)
    private String billingCycle = "MONTHLY";
}

