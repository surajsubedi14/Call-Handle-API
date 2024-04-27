package com.example.CallHandleAPI.DTO;

import lombok.Data;

@Data
public class PatientRemoveDTO {
    private Long patientId;
    private Long doctorId;
    private Long ehrId;
}
