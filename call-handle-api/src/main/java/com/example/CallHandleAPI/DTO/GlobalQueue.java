package com.example.CallHandleAPI.DTO;
import lombok.Data;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.*;

@Component
@Data
public class GlobalQueue {

    // To track incoming patient queue
    private List<PatientDoctorDTO> globalQueue;

    // To track queue position
    private List<Long> tickets;

    @PostConstruct
    public void init() {
        globalQueue = new LinkedList<>();
        tickets = new ArrayList<>();
    }

}

