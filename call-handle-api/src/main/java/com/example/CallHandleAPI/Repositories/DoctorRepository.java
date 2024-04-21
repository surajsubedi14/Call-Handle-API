package com.example.CallHandleAPI.Repositories;

import com.example.CallHandleAPI.Models.Doctor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends CrudRepository<Doctor, Integer> {

    @Query("select d from Doctor d where d.user_id = ?1")
    Optional<Doctor> findById(long id);
}
