package com.example.CallHandleAPI.Models;


import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Data
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long department_id;

    private String department_name;

    @ManyToMany(mappedBy = "department")
    Set<Hospital> hospital;


}