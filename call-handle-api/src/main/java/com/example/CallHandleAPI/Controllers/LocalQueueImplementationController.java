package com.example.CallHandleAPI.Controllers;

import com.example.CallHandleAPI.DTO.LocalQueue;
import com.example.CallHandleAPI.DTO.PatientDoctorDTO;
import com.example.CallHandleAPI.DTO.PatientRemoveDTO;
import com.example.CallHandleAPI.Models.Patient;
import com.example.CallHandleAPI.Services.DoctorServices;
import com.example.CallHandleAPI.Services.PatientServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;
@CrossOrigin
@RestController
@RequestMapping("/local")
public class LocalQueueImplementationController {

    @Autowired
    private DoctorServices doctorServices;

    @Autowired
    private PatientServices patientServices;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final GlobalQueueImplementationController globalQueueImplementationController;
    private final LocalQueue localQueue;

    @Autowired
    public LocalQueueImplementationController(GlobalQueueImplementationController globalQueueImplementationController, LocalQueue localQueue) {
        this.globalQueueImplementationController = globalQueueImplementationController;
        this.localQueue = localQueue;
    }

    @GetMapping(value = "/queue")
    public ResponseEntity<?> GetLocalQueue() {
        try {
            return ResponseEntity.ok(localQueue.getLocalQueue());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage());
        }
    }

    @GetMapping(value = "/tickets")
    public Map<Long, Integer> GetLocalQueueTickets() {
        try {
            return localQueue.getTickets();
        } catch (Exception e) {
            return new HashMap<Long, Integer>();
        }
    }

    public Long GetFrontPatient(Long doctorId) {
        if(!localQueue.getLocalQueue().isEmpty()) {
            return localQueue.getLocalQueue().get(doctorId).get(0);
        }
        return (long) -1;
    }

    @GetMapping(value = "/assigned-doctor")
    public Map<Long, Long> GetAssignedDoctors() {
        try {
            return localQueue.getAssignedDoctor();
        } catch (Exception e) {
            return new HashMap<Long, Long>();
        }
    }

    @GetMapping(value = "/size/{id}")
    public ResponseEntity<?> GetLocalQueueSize(@PathVariable long id) {
        try {
            return ResponseEntity.ok(localQueue.getLocalQueue().get(id).size());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage());
        }
    }

    @PostMapping(value = "/add-to-queue")
    public ResponseEntity<?> AddToLocalQueue(@RequestBody PatientDoctorDTO patientDoctor) {
        try {
            if(patientDoctor.getPatientId() == null) {
                return ResponseEntity.badRequest().body("No patient given");
            }
            Long patientId = patientDoctor.getPatientId();
            long targetDoctor = -1;
            int minimum = Integer.MAX_VALUE;
            for (Map.Entry<Long, List<Long>> entry : localQueue.getLocalQueue().entrySet()) {
                if (entry.getValue().size() != LocalQueue.LOCAL_QUEUE_SIZE) {
                    if(minimum > entry.getValue().size()) {
                        targetDoctor = entry.getKey();
                        minimum = entry.getValue().size();
                    }
                }
            }
            if(targetDoctor == (long) -1) {
                PatientDoctorDTO patientDoctorDTO = new PatientDoctorDTO();
                patientDoctorDTO.setPatientId(patientId);
                patientDoctorDTO.setDoctorId(null);
                globalQueueImplementationController.AddToGlobalQueue(patientDoctorDTO);
            } else {
                if(localQueue.getLocalQueue().get(targetDoctor).isEmpty()) {
                    messagingTemplate.convertAndSend("/topic/call-incoming/" + targetDoctor, patientDoctor.getPatientId());
                }
                localQueue.getLocalQueue().get(targetDoctor).add(patientId);
                int position = localQueue.getLocalQueue().get(targetDoctor).size() - 1;
                localQueue.getTickets().put(patientId, position);
                messagingTemplate.convertAndSend("/topic/next-patients/" + targetDoctor, localQueue.getLocalQueue().get(targetDoctor));
            }
            return ResponseEntity.ok(doctorServices.getDoctor(targetDoctor));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage());
        }
    }

    @PostMapping(value = "/add-to-queue-sd")
    public ResponseEntity<?> AddToLocalQueueSD(@RequestBody PatientDoctorDTO patientDoctorDTO) {
        try {
            if(patientDoctorDTO.getPatientId() == null || patientDoctorDTO.getDoctorId() == null) {
                return ResponseEntity.badRequest().body("No patient or doctor given");
            }
            long targetDoctor = patientDoctorDTO.getDoctorId();

            if(localQueue.getLocalQueue().get(targetDoctor).size() == LocalQueue.LOCAL_QUEUE_SIZE) {
                globalQueueImplementationController.AddToGlobalQueue(patientDoctorDTO);
            } else {
                if(localQueue.getLocalQueue().get(targetDoctor).isEmpty()) {
                    messagingTemplate.convertAndSend("/topic/call-incoming/" + targetDoctor, patientDoctorDTO.getPatientId());
                }
                localQueue.getLocalQueue().get(targetDoctor).add(patientDoctorDTO.getPatientId());
                int position = localQueue.getLocalQueue().get(targetDoctor).size();
                localQueue.getTickets().put(patientDoctorDTO.getPatientId(), position);
                messagingTemplate.convertAndSend("/topic/next-patients/" + targetDoctor, localQueue.getLocalQueue().get(targetDoctor));
            }
            return ResponseEntity.ok(doctorServices.getDoctor(patientDoctorDTO.getDoctorId()));
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

            if(localQueue.getTickets().get(patientDoctorDTO.getPatientId()) == null) {
                return globalQueueImplementationController.MarkAsNotWaiting(patientDoctorDTO);
            }

            List<Long> patientQueue = localQueue.getLocalQueue().get(patientDoctorDTO.getDoctorId());
            boolean found = false;
            for (Long patientId : patientQueue) {
                if (found) {
                    localQueue.getTickets().put(patientId, localQueue.getTickets().get(patientId) - 1);
                }
                if (Objects.equals(patientId, patientDoctorDTO.getPatientId())) {
                    found = true;
                }
            }

            //Remove patient from queue
            localQueue.getLocalQueue().get(patientDoctorDTO.getDoctorId()).remove(patientDoctorDTO.getPatientId());

            //Remove his ticket
            localQueue.getTickets().remove(patientDoctorDTO.getPatientId());

            PatientDoctorDTO patientDoctorDTO1 = globalQueueImplementationController.PromoteToLocalQueue(patientDoctorDTO);

            if(patientDoctorDTO1.getPatientId() == null) {
                messagingTemplate.convertAndSend("/topic/next-patients/" + patientDoctorDTO.getDoctorId(), localQueue.getLocalQueue().get(patientDoctorDTO.getDoctorId()));
                return ResponseEntity.ok("Nothing in Global queue to promote to local queue");
            }

            localQueue.getLocalQueue().get(patientDoctorDTO.getDoctorId()).add(patientDoctorDTO1.getPatientId());

            localQueue.getTickets().put(patientDoctorDTO1.getPatientId(), LocalQueue.LOCAL_QUEUE_SIZE-1);

            localQueue.getAssignedDoctor().put(patientDoctorDTO1.getPatientId(), patientDoctorDTO.getDoctorId());

            messagingTemplate.convertAndSend("/topic/next-patients/" + patientDoctorDTO.getDoctorId(), localQueue.getLocalQueue().get(patientDoctorDTO.getDoctorId()));

            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage());
        }
    }

    @PostMapping(value = "/get-position")
    public Integer GetLocalQueuePosition(@RequestBody PatientDoctorDTO patientDoctorDTO) {
        try {
            if(patientDoctorDTO.getPatientId() == null) {
                return -1;
            }
            Long patientId = patientDoctorDTO.getPatientId();
            return localQueue.getTickets().get(patientId);
        } catch (Exception e) {
            return e.hashCode();
        }
    }

    @PostMapping(value = "/exist-in-queue")
    public ResponseEntity<?> ExistsInLocalQueue(@RequestBody PatientDoctorDTO patientDoctorDTO) {
        try {
            if(patientDoctorDTO.getPatientId() == null) {
                return ResponseEntity.badRequest().body("No patient given");
            }
            return ResponseEntity.ok(localQueue.getTickets().get(patientDoctorDTO.getPatientId()) != 0);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage());
        }
    }

    @GetMapping(value = "/queue/next/{id}")
    public ResponseEntity<?> NextInLocalQueue(@PathVariable Long id) {
        try {
            if(id == null) {
                return ResponseEntity.badRequest().body("No doctor given");
            }
            return ResponseEntity.ok(localQueue.getLocalQueue().get(id));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage());
        }
    }

    @PostMapping(value = "/assign-doctor")
    public ResponseEntity<?> AssignDoctor(@RequestBody PatientDoctorDTO patientDoctor) {
        try {
            if(patientDoctor.getDoctorId() == null) {
                return ResponseEntity.badRequest().body("No doctor given");
            }
            Long doctorId = patientDoctor.getDoctorId();
            List<Long> patientQueue = localQueue.getLocalQueue().get(doctorId);
            for(int i=0; i<patientQueue.size(); i++) {
                localQueue.getTickets().put(patientQueue.get(i), localQueue.getTickets().get(patientQueue.get(i)) - 1);
            }

            //Get patient from queue
            Optional<Patient> patient = patientServices.getPatient(localQueue.getLocalQueue().get(doctorId).get(0));

            if(patient.isPresent()) {

                PatientDoctorDTO temp = new PatientDoctorDTO();
                temp.setDoctorId(patientDoctor.getDoctorId());
                temp.setPatientId(patient.get().getUser_id());
                return ResponseEntity.ok(temp);
            }

            return ResponseEntity.ok("Local Queue empty.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage());
        }
    }

    @PostMapping(value = "/remove")
    public ResponseEntity<?> RemovePatient(@RequestBody PatientRemoveDTO patientDoctor) {
        try {
            if(patientDoctor.getDoctorId() == null) {
                return ResponseEntity.badRequest().body("No doctor given");
            }
            Long doctorId = patientDoctor.getDoctorId();

            //Remove patient from queue
            Long patientId = localQueue.getLocalQueue().get(doctorId).remove(0);
            patientDoctor.setPatientId(patientId);

            if(patientId != 0) {
                //Remove his ticket
                localQueue.getTickets().remove(patientId);
                localQueue.getAssignedDoctor().remove(patientId);

                PatientDoctorDTO pD = new PatientDoctorDTO();
                pD.setDoctorId(doctorId);

                PatientDoctorDTO result = globalQueueImplementationController.PromoteToLocalQueue(pD);

                if(result.getPatientId() != null) {
                    localQueue.getLocalQueue().get(doctorId).add(result.getPatientId());
                    localQueue.getTickets().put(result.getPatientId(), LocalQueue.LOCAL_QUEUE_SIZE);
                    localQueue.getAssignedDoctor().put(result.getPatientId(), doctorId);
                }

                // Send WebSocket message containing the next ID in the local queue
                if(!localQueue.getLocalQueue().get(doctorId).isEmpty()) {
                    Long nextPatientId = localQueue.getLocalQueue().get(doctorId).get(0);
                    messagingTemplate.convertAndSend("/topic/next-id", nextPatientId);
                }

                messagingTemplate.convertAndSend("/topic/next-patients/" + patientDoctor.getDoctorId(), localQueue.getLocalQueue().get(patientDoctor.getDoctorId()));
                messagingTemplate.convertAndSend("/topic/get-prescription-request/" + patientDoctor.getDoctorId(), patientDoctor);

                return ResponseEntity.ok(true);
            }

            return ResponseEntity.ok("Local Queue empty.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Internal Server Error: " + e.getMessage());
        }
    }
}
