package com.example.CallHandleAPI.Services;

import com.example.CallHandleAPI.Models.Patient;
import com.example.CallHandleAPI.Repositories.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PatientServices {

    @Autowired
    private PatientRepository patientRepository;

    public Optional<Patient> getPatient (long id ) { return patientRepository.findById(id); }
}
