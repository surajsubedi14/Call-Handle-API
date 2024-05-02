package com.example.CallHandleAPI.Controllers;

import com.example.CallHandleAPI.DTO.LocalQueue;
import com.example.CallHandleAPI.DTO.PatientDoctorDTO;
import com.example.CallHandleAPI.DTO.WaitingPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

@Controller
public class LocalQueuePolling {

    private final LocalQueueImplementationController localQueueImplementationController;
    private final GlobalQueueImplementationController globalQueueImplementationController;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public LocalQueuePolling(LocalQueueImplementationController localQueueImplementationController,
                             GlobalQueueImplementationController globalQueueImplementationController,
                             SimpMessagingTemplate messagingTemplate) {
        this.localQueueImplementationController = localQueueImplementationController;
        this.globalQueueImplementationController = globalQueueImplementationController;
        this.simpMessagingTemplate = messagingTemplate;
    }

    @MessageMapping("/send-data/{queueId}")
    @SendTo("/topic/next-id/{queueId}")
    public ResponseEntity<?> PollDoctorQueue(@DestinationVariable String queueId) {
        PatientDoctorDTO input = new PatientDoctorDTO();
        input.setDoctorId(Long.valueOf(queueId));
        input.setPatientId(null);
        return ResponseEntity.ok(localQueueImplementationController.AssignDoctor(input));
    }

    @MessageMapping("/reload-position")
    public void GetWaitingPosition() {
        Map<Long, Integer> localTickets = localQueueImplementationController.GetLocalQueueTickets();
        Map<Long, Long> assigned = localQueueImplementationController.GetAssignedDoctors();
        for(Map.Entry<Long, Integer> element : localTickets.entrySet()) {
            WaitingPosition waitingPosition = new WaitingPosition();
            waitingPosition.setDoctorId(assigned.get(element.getKey()));
            waitingPosition.setPosition(element.getValue());
            simpMessagingTemplate.convertAndSend("/topic/get-position/" + element.getKey(), waitingPosition);
        }
        List<Long> globalTickets = globalQueueImplementationController.GetGlobalQueueTickets();
        for(int i=0; i<globalTickets.size(); i++) {
            int pos = i+1 + (LocalQueue.LOCAL_QUEUE_SIZE - 1);
            WaitingPosition waitingPosition = new WaitingPosition();
            waitingPosition.setPosition(pos);
            waitingPosition.setDoctorId(null);
            simpMessagingTemplate.convertAndSend("/topic/get-position/" + globalTickets.get(i), waitingPosition);
        }
    }

    @MessageMapping("/send-consent-request/{doctorId}")
    public void SendConsentRequest(@DestinationVariable String doctorId) {
        Long patientId = localQueueImplementationController.GetFrontPatient(Long.valueOf(doctorId));
        simpMessagingTemplate.convertAndSend("/topic/get-consent-request/" + patientId, doctorId);
    }

    @MessageMapping("/consent-reply/{doctorId}")
    public void SendConsentReply(@DestinationVariable String doctorId, @Payload Boolean consent) {
        simpMessagingTemplate.convertAndSend("/topic/get-consent-reply/" + doctorId, consent);
    }

    @MessageMapping("/monitor-start/{doctorId}")
    public void SendMonitorSituation(@DestinationVariable String doctorId) {
        simpMessagingTemplate.convertAndSend("/topic/senior-monitor/" + doctorId, doctorId);
    }


}
