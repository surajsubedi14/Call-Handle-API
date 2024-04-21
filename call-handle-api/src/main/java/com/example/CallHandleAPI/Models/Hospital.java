package com.example.CallHandleAPI.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Entity
@Data
public class Hospital {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hospital_id;
    private String name;
    private String email;
    private String phoneNumber;
    private String address;
    private String registrationNumber;
    private String imgUrl;
    private float rating;
    private String website;
    private String password;
    private String pincode;

    private String role;

    private String category;

    @OneToMany(mappedBy = "hospital")
    private List<Doctor> doctor;


    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "Hospital_Department",
            joinColumns = @JoinColumn(name = "hospital_id"),
            inverseJoinColumns = @JoinColumn(name = "department_id"))
    @JsonIgnore
    Set<Department> department;

}
