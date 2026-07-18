package com.employeeresumemaneger.controller;

import com.employeeresumemaneger.config.RabbitMqConfig;
import com.employeeresumemaneger.dto.ResumeUploadedEvent;
import com.employeeresumemaneger.model.Candidate;
import com.employeeresumemaneger.model.Employee;
import com.employeeresumemaneger.model.Resume;
import com.employeeresumemaneger.repository.CandidateRepository;
import com.employeeresumemaneger.service.EmployeeService;
import com.employeeresumemaneger.service.MinioService;
import com.employeeresumemaneger.service.PdfService;
import com.employeeresumemaneger.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Controller
@RequestMapping("/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController {

    private final EmployeeService employeeService;
    private final ResumeService resumeService;
    private final PdfService pdfService;
    private final MinioService minioService;
    private final RabbitTemplate rabbitTemplate;
    private final CandidateRepository candidateRepository;

    @GetMapping
    public String listEmployees(Model model) {
        log.info("[EMPLOYEE-CONTROLLER] GET /employees -> Çalışan listesi sayfası istendi");
        List<Employee> employees = employeeService.findAll();
        model.addAttribute("employees", employees);
        log.info("[EMPLOYEE-CONTROLLER] {} çalışan listeleniyor", employees.size());
        return "employee/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        log.info("[EMPLOYEE-CONTROLLER] GET /employees/new -> Yeni çalışan formu gösteriliyor");
        model.addAttribute("employee", new Employee());
        return "employee/form";
    }
    @GetMapping("/upload")
    public String uploadResumeForm(Model model) {
        model.addAttribute("candidate", new Candidate());
        return "resume/upload";
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
            rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE_NAME, RabbitMqConfig.ROUTING_KEY,
                    ResumeUploadedEvent.builder()
                            .candidate(candidate)
                            .extractedText(extractedText)
                            .build());

            redirectAttributes.addFlashAttribute("info", "Basvurunuz basariyla alinmistir");
            return "redirect:/list";

        } catch (IOException exception) {
            log.error("PDF metni cikarilirken hata olustu: {}", exception.getMessage());
            redirectAttributes.addFlashAttribute("error", "PDF okunamadi, dosya bozuk olabilir");
            return "redirect:/upload";
        }
    }
    @PostMapping
    public String createEmployee(@ModelAttribute Employee employee,
                                  @RequestParam(value = "resumeFile", required = false) MultipartFile resumeFile,
                                  RedirectAttributes redirectAttributes) {
        log.info("[EMPLOYEE-CONTROLLER] POST /employees -> Yeni çalışan kaydediliyor: {} {}",
                employee.getFirstName(), employee.getLastName());
        try {
            Employee saved = employeeService.save(employee);
            log.info("[EMPLOYEE-CONTROLLER] Çalışan başarıyla kaydedildi -> id: {}", saved.getId());

            if (resumeFile != null && !resumeFile.isEmpty()) {
                log.info("[EMPLOYEE-CONTROLLER] CV dosyası yükleniyor -> dosya: {}, boyut: {} bytes",
                        resumeFile.getOriginalFilename(), resumeFile.getSize());

                String storageKey = "resumes/employee-%d/%d-%s"
                        .formatted(saved.getId(), System.currentTimeMillis(), resumeFile.getOriginalFilename());

                minioService.upload(resumeFile, storageKey);
                resumeService.uploadAndNotify(saved.getId(), resumeFile.getOriginalFilename(),
                        storageKey, resumeFile.getContentType(), resumeFile.getSize());

                redirectAttributes.addFlashAttribute("successMessage",
                        "Çalışan ve CV başarıyla kaydedildi! CV işleme kuyruğuna eklendi.");
                log.info("[EMPLOYEE-CONTROLLER] Çalışan + CV başarıyla kaydedildi -> employeeId: {}", saved.getId());
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Çalışan başarıyla oluşturuldu!");
            }

        } catch (Exception e) {
            log.error("[EMPLOYEE-CONTROLLER] Çalışan kaydedilirken hata oluştu!", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Çalışan kaydedilirken hata oluştu: " + e.getMessage());
        }
        return "redirect:/employees";
    }

    @GetMapping("/{id}")
    public String showEmployee(@PathVariable Long id, Model model) {
        log.info("[EMPLOYEE-CONTROLLER] GET /employees/{} -> Çalışan detayı istendi", id);
        Employee employee = employeeService.findById(id);
        model.addAttribute("employee", employee);
        log.info("[EMPLOYEE-CONTROLLER] Çalışan detayı gösteriliyor: {} {}, CV sayısı: {}",
                employee.getFirstName(), employee.getLastName(),
                employee.getResumes() != null ? employee.getResumes().size() : 0);
        return "employee/detail";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        log.info("[EMPLOYEE-CONTROLLER] GET /employees/{}/edit -> Düzenleme formu gösteriliyor", id);
        Employee employee = employeeService.findById(id);
        model.addAttribute("employee", employee);
        return "employee/form";
    }

    @PostMapping("/{id}")
    public String updateEmployee(@PathVariable Long id, @ModelAttribute Employee employee,
            RedirectAttributes redirectAttributes) {
        log.info("[EMPLOYEE-CONTROLLER] POST /employees/{} -> Çalışan güncelleniyor", id);
        try {
            employeeService.update(id, employee);
            redirectAttributes.addFlashAttribute("successMessage", "Çalışan başarıyla güncellendi!");
            log.info("[EMPLOYEE-CONTROLLER] Çalışan güncellendi, detay sayfasına yönlendiriliyor");
        } catch (Exception e) {
            log.error("[EMPLOYEE-CONTROLLER] Çalışan güncellenirken hata oluştu! id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Güncelleme hatası: " + e.getMessage());
        }
        return "redirect:/employees/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("[EMPLOYEE-CONTROLLER] POST /employees/{}/delete -> Çalışan siliniyor", id);
        try {
            employeeService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Çalışan başarıyla silindi!");
            log.info("[EMPLOYEE-CONTROLLER] Çalışan silindi, listeye yönlendiriliyor");
        } catch (Exception e) {
            log.error("[EMPLOYEE-CONTROLLER] Çalışan silinirken hata oluştu! id: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Silme hatası: " + e.getMessage());
        }
        return "redirect:/employees";
    }
}
