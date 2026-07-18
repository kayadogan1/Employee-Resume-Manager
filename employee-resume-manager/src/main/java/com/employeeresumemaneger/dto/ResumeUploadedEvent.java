package com.employeeresumemaneger.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeUploadedEvent {
    private Long candidateId;
    private String extractedText;
    private String storageKey;

}
