package com.example.CallHandleAPI.DTO;

import com.example.CallHandleAPI.Models.Doctor;
import com.example.CallHandleAPI.Services.DoctorServices;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;

@Component
@Data
public class LocalQueue {

    public static final int LOCAL_QUEUE_SIZE = 2;

    // To track incoming patient local queue
    private Map<Long, List<Long>> localQueue;

    // To track queue position
    private Map<Long, Integer> tickets;

    // To track assigned doctor
    private Map<Long, Long> assignedDoctor;

    @Autowired
    private DoctorServices doctorServices;

    @PostConstruct
    public void init() {

        List<Doctor> allDoctors = (List<Doctor>) doctorServices.getAllDoctors();

        localQueue = new HashMap<>();
        tickets = new HashMap<>();
        assignedDoctor = new HashMap<>();

        for (Doctor allDoctor : allDoctors) {
            localQueue.put(allDoctor.getUser_id(), new ArrayList<>(LOCAL_QUEUE_SIZE));
        }
    }
}
