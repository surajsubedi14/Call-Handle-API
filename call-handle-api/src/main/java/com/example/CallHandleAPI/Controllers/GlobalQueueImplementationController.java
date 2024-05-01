package com.example.CallHandleAPI.Controllers;

import com.example.CallHandleAPI.DTO.GlobalQueue;
import com.example.CallHandleAPI.DTO.PatientDoctorDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@CrossOrigin
@RequestMapping("/global")
public class GlobalQueueImplementationController {

    private final GlobalQueue globalQueue;

    @Autowired
    public GlobalQueueImplementationController(GlobalQueue globalQueue) {
        this.globalQueue = globalQueue;
    }

    @GetMapping(value = "/size")
    public ResponseEntity<?> GetGlobalQueueSize() {
        try {
            return ResponseEntity.ok(globalQueue.getGlobalQueue().size());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage());
        }
    }

    @GetMapping(value = "/queue")
    public ResponseEntity<?> GetGlobalQueue() {
        try {
            return ResponseEntity.ok(globalQueue.getGlobalQueue());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage());
        }
    }

    @GetMapping(value = "/tickets")
    public List<Long> GetGlobalQueueTickets() {
        try {
            return globalQueue.getTickets();
        } catch (Exception e) {
            return new ArrayList<Long>();
        }
    }

    @PostMapping(value = "/add-to-queue")
    public ResponseEntity<?> AddToGlobalQueue(@RequestBody PatientDoctorDTO patientDoctorDTO) {
        try {
            if(patientDoctorDTO.getPatientId() == null) {
                return ResponseEntity.badRequest().body("No patient given");
            }
            //Add to queue
            List<PatientDoctorDTO> temp = globalQueue.getGlobalQueue();
            temp.add(patientDoctorDTO);
            globalQueue.setGlobalQueue(temp);

            //Assign a ticket
            globalQueue.getTickets().add(patientDoctorDTO.getPatientId());

            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage());
        }
    }

    @PostMapping(value = "/cancel-waiting")
    public ResponseEntity<?> MarkAsNotWaiting(@RequestBody PatientDoctorDTO patientDoctorDTO) {
        try {
            if(patientDoctorDTO.getPatientId() == null) {
                return ResponseEntity.badRequest().body("No patient given");
            }
            //Remove patient from queue
            globalQueue.getGlobalQueue().remove(patientDoctorDTO);

            //Remove his ticket
            globalQueue.getTickets().remove(patientDoctorDTO.getPatientId());

            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage());
        }
    }

    @PostMapping(value = "/get-position")
    public Integer GetGlobalQueuePosition(@RequestBody PatientDoctorDTO patientDoctorDTO) {
        try {
            if(patientDoctorDTO.getPatientId() == null) {
                return ResponseEntity.badRequest().body("No patient given").getStatusCodeValue();
            }
            Long patientId = patientDoctorDTO.getPatientId();
            int position = 0;
            for(int i=0; i<globalQueue.getTickets().size(); i++) {
                if(Objects.equals(globalQueue.getTickets().get(i), patientId)) {
                    return position;
                }
            }
            return -1;
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage()).getStatusCodeValue();
        }
    }

    @PostMapping(value = "/exist-in-queue")
    public ResponseEntity<?> ExistsInGlobalQueue(@RequestBody PatientDoctorDTO patientDoctorDTO) {
        try {
            if(patientDoctorDTO.getPatientId() == null) {
                return ResponseEntity.badRequest().body("No patient or doctor given");
            }
            return ResponseEntity.ok(globalQueue.getGlobalQueue().contains(patientDoctorDTO));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage());
        }
    }

    @PostMapping(value = "/promote")
    public PatientDoctorDTO PromoteToLocalQueue(@RequestBody PatientDoctorDTO patientDoctor) {
        if(patientDoctor.getDoctorId() == null) {
            return new PatientDoctorDTO();
        }
        Long doctorId = patientDoctor.getDoctorId();
        if(globalQueue.getTickets().isEmpty()) {
            return new PatientDoctorDTO();
        }

        PatientDoctorDTO patientDoctorDTO = null;
        for(int i=0; i<globalQueue.getGlobalQueue().size(); i++) {
            if(globalQueue.getGlobalQueue().get(i).getDoctorId() == null || Objects.equals(globalQueue.getGlobalQueue().get(i).getDoctorId(), doctorId)) {
                patientDoctorDTO = globalQueue.getGlobalQueue().get(i);
                break;
            }
        }

        if(patientDoctorDTO == null) {
            return new PatientDoctorDTO();
        }

        globalQueue.getTickets().remove(patientDoctorDTO.getPatientId());

        return globalQueue.getGlobalQueue().remove(globalQueue.getGlobalQueue().indexOf(patientDoctorDTO));
    }
}
