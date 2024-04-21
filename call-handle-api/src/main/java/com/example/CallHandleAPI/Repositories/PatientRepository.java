package com.example.CallHandleAPI.Repositories;

import com.example.CallHandleAPI.Models.Patient;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends CrudRepository<Patient, Integer> {

    @Query("select p from Patient p where p.user_id = ?1")
    Optional<Patient> findById(long id);
}
