package com.employeeresumemaneger.service;

import com.employeeresumemaneger.config.RabbitMqConfig;
import com.employeeresumemaneger.dto.ResumeUploadedEvent;
import com.employeeresumemaneger.model.Employee;
import com.employeeresumemaneger.model.Resume;
import com.employeeresumemaneger.repository.ResumeRepository;
import com.employeeresumemaneger.util.ResumeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final MinioService minioService;


    public Resume findById(Long id) {
        log.info("[RESUME-SERVICE] CV aranıyor -> id: {}", id);
        Resume resume = resumeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[RESUME-SERVICE] CV bulunamadı! id: {}", id);
                    return new RuntimeException("CV bulunamadı: id=" + id);
                });
        log.info("[RESUME-SERVICE] CV bulundu -> id: {}, dosya: {}", id, resume.getOriginalFileName());
        return resume;
    }

    public Resume save(Resume resume) {
        log.info("[RESUME-SERVICE] CV kaydediliyor -> dosya: {}, storageKey: {}",
                resume.getOriginalFileName(), resume.getStorageKey());
        Resume saved = resumeRepository.save(resume);
        log.info("[RESUME-SERVICE] CV veritabanına kaydedildi -> id: {}", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        log.info("[RESUME-SERVICE] CV siliniyor -> id: {}", id);
        Resume resume = findById(id);

        try {
            minioService.delete(resume.getStorageKey());
            log.info("[RESUME-SERVICE] CV dosyası MinIO'dan silindi -> storageKey: {}", resume.getStorageKey());
        } catch (Exception e) {
            log.error("[RESUME-SERVICE] MinIO'dan dosya silinirken hata oluştu! storageKey: {}",
                    resume.getStorageKey(), e);
        }

        resumeRepository.delete(resume);
        log.info("[RESUME-SERVICE] CV veritabanından silindi -> id: {}", id);
    }

    public List<Resume> findAll() {
        log.info("[RESUME-SERVICE] Tüm CV'ler listeleniyor...");
        List<Resume> resumes = resumeRepository.findAll();
        log.info("[RESUME-SERVICE] {} adet CV bulundu.", resumes.size());
        return resumes;
    }
}
