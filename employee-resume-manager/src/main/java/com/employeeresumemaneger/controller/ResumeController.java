package com.employeeresumemaneger.controller;

import com.employeeresumemaneger.config.RabbitMqConfig;
import com.employeeresumemaneger.dto.ResumeUploadedEvent;
import com.employeeresumemaneger.model.Candidate;
import com.employeeresumemaneger.model.Resume;
import com.employeeresumemaneger.repository.CandidateRepository;
import com.employeeresumemaneger.service.MinioService;
import com.employeeresumemaneger.service.PdfService;
import com.employeeresumemaneger.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.InputStream;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ResumeController {

    private final ResumeService resumeService;
    private final MinioService minioService;
    private final CandidateRepository candidateRepository;
    private final RabbitTemplate rabbitTemplate;
    private final PdfService pdfService;

    @GetMapping("/resumes")
    public String listAllResumes(Model model) {
        log.info("[RESUME-CONTROLLER] GET /resumes -> Tüm CV listesi istendi");
        List<Resume> resumes = resumeService.findAll();

        model.addAttribute("resumes", resumes);
        log.info("[RESUME-CONTROLLER] {} adet CV listeleniyor", resumes.size());
        return "resume/list";
    }
    @GetMapping("/upload")
    public String uploadResumeForm(Model model) {
        log.info("[RESUME-CONTROLLER] GET /upload -> Yukleme formu gosteriliyor");
        model.addAttribute("candidate", new Candidate());
        return "resume/createResume";  // templates/resume/upload.html
    }
    @PostMapping("/upload")
    public String uploadResumeFile(@ModelAttribute Candidate candidate,
                                   @RequestParam("resumeFile") MultipartFile file,
                                   RedirectAttributes redirectAttributes) {

        if (file.isEmpty()) {
            log.error("File is empty");
            redirectAttributes.addFlashAttribute("error", "Lutfen bir PDF secin");
            return "redirect:/upload";
        }

        try {
            String extractedText = pdfService.extractText(file);
            log.info("PDF'den {} karakter metin cikarildi", extractedText.length());

            candidateRepository.save(candidate);

            String storageKey = candidate.getId() + "_" + file.getOriginalFilename();
            minioService.upload(file, storageKey);

            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.ROUTING_KEY,
                    ResumeUploadedEvent.builder()
                            .candidateId(candidate.getId())
                            .extractedText(extractedText)
                            .storageKey(storageKey)
                            .originalFileName(file.getOriginalFilename())
                            .contentType(file.getContentType())
                            .fileSizeBytes(file.getSize())
                            .build());

            redirectAttributes.addFlashAttribute("info", "Basvurunuz basariyla alinmistir");
            return "redirect:/upload";

        } catch (Exception exception) {
            log.error("Yukleme sirasinda hata olustu: {}", exception.getMessage(), exception);
            redirectAttributes.addFlashAttribute("error", "Bir hata olustu, lutfen tekrar deneyin");
            return "redirect:/upload";
        }
    }

    @GetMapping("/resumes/{id}/download")
    public ResponseEntity<InputStreamResource> downloadResume(@PathVariable Long id) {
        log.info("[RESUME-CONTROLLER] GET /resumes/{}/download -> CV indirme istendi", id);

        try {
            Resume resume = resumeService.findById(id);
            InputStream inputStream = minioService.download(resume.getStorageKey());

            log.info("[RESUME-CONTROLLER] CV indiriliyor -> dosya: {}", resume.getOriginalFileName());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(
                            resume.getContentType() != null ? resume.getContentType() : "application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resume.getOriginalFileName() + "\"")
                    .body(new InputStreamResource(inputStream));

        } catch (Exception e) {
            log.error("[RESUME-CONTROLLER] CV indirilirken hata oluştu! resumeId: {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/resumes/{id}/delete")
    public String deleteResume(@PathVariable Long id,
                                @RequestParam(value = "employeeId", required = false) Long employeeId,
                                RedirectAttributes redirectAttributes) {
        log.info("[RESUME-CONTROLLER] POST /resumes/{}/delete -> CV siliniyor", id);

        try {
            resumeService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "CV başarıyla silindi!");
            log.info("[RESUME-CONTROLLER] CV silindi -> resumeId: {}", id);
        } catch (Exception e) {
            log.error("[RESUME-CONTROLLER] CV silinirken hata oluştu! resumeId: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "CV silinemedi: " + e.getMessage());
        }

        if (employeeId != null) {
            return "redirect:/employees/" + employeeId;
        }
        return "redirect:/resumes";
    }
}
