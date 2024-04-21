package com.example.CallHandleAPI.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "doctor")
@PrimaryKeyJoinColumn(name = "user_id")
public class Doctor extends User {

    private String registrationNumber;
    private String degree;
    private String availability;
    private String seniorityLevel;
    private int rating;

    private String specialization;

    private int yearOfExp;

    @ManyToOne
    @JoinColumn(name = "hospital_id")
    @JsonIgnore
    private Hospital hospital;

}
