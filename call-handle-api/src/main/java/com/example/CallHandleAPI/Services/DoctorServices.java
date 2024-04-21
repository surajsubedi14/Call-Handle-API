package com.example.CallHandleAPI.Services;

import com.example.CallHandleAPI.Models.Doctor;
import com.example.CallHandleAPI.Repositories.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DoctorServices {

    @Autowired
    private DoctorRepository doctorRepository;

    public long countDoctors () { return doctorRepository.count(); }

    public Iterable<Doctor> getAllDoctors () { return doctorRepository.findAll(); }

    public Optional<Doctor> getDoctor (long id ) { return doctorRepository.findById(id); }
}
