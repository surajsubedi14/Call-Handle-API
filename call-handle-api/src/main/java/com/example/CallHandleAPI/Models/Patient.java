package com.example.CallHandleAPI.Models;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name ="patient")
@PrimaryKeyJoinColumn(name = "user_id")
public class Patient extends User {

    private double weight;
    private double height;
    private String bloodGroup;
    private String document_url;

}

