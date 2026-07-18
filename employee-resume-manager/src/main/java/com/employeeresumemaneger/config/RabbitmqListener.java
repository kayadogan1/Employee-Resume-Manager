package com.employeeresumemaneger.config;

import com.employeeresumemaneger.dto.ResumeUploadedEvent;
import com.employeeresumemaneger.model.Candidate;
import com.employeeresumemaneger.model.Resume;
import com.employeeresumemaneger.repository.ResumeRepository;
import com.employeeresumemaneger.util.ResumeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class RabbitmqListener {

    private final ResumeRepository resumeRepository;

    @RabbitListener(queues = RabbitMqConfig.QUEUE_NAME)
    public void handleResumeUploadProcess(ResumeUploadedEvent event) {
        log.info("[RABBITMQ-LISTENER] ===== CV ISLEME BASLADI =====");
        log.info("[RABBITMQ-LISTENER] Event alindi -> {}", event);

        try {
            Candidate candidate = new Candidate();
            candidate.setId(event.getCandidateId());

            Resume resume = new Resume();
            resume.setCandidate(candidate);
            resume.setParsedText(event.getExtractedText());
            resume.setStorageKey(event.getStorageKey());
            resume.setStatus(ResumeStatus.PROCESSED);
            resume.setProcessedAt(Instant.now());

            resumeRepository.save(resume);

            log.info("[RABBITMQ-LISTENER] Resume basariyla islendi, id: {}", resume.getId());

        } catch (Exception e) {
            log.error("[RABBITMQ-LISTENER] Mesaj islenirken hata olustu: {}", e.getMessage(), e);
        }

        log.info("[RABBITMQ-LISTENER] ===== CV ISLEME TAMAMLANDI =====");
    }
}
