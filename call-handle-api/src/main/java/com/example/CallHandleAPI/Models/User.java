package com.example.CallHandleAPI.Models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Inheritance(strategy = InheritanceType.JOINED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long user_id;

    private String firstName;

    private String lastName;

    @Column(unique = true)
    private String phoneNumber;

    @Column(unique = true)
    private String email;

    private int age;

    private String gender;

    private String aadhaar;

    private String role;

    private String state;

    private String city;

    private String password;

    private String img_url;

    private boolean active;
}
