package com.employeeresumemaneger.service;

import com.employeeresumemaneger.model.Employee;
import com.employeeresumemaneger.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public List<Employee> findAll() {
        log.info("[EMPLOYEE-SERVICE] Tüm çalışanlar listeleniyor...");
        List<Employee> employees = employeeRepository.findAll();
        log.info("[EMPLOYEE-SERVICE] {} adet çalışan bulundu.", employees.size());
        return employees;
    }

    public Employee findById(Long id) {
        log.info("[EMPLOYEE-SERVICE] Çalışan aranıyor -> id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("[EMPLOYEE-SERVICE] Çalışan bulunamadı! id: {}", id);
                    return new RuntimeException("Çalışan bulunamadı: id=" + id);
                });
        log.info("[EMPLOYEE-SERVICE] Çalışan bulundu -> {} {}", employee.getFirstName(), employee.getLastName());
        return employee;
    }

    public Employee save(Employee employee) {
        log.info("[EMPLOYEE-SERVICE] Yeni çalışan kaydediliyor -> {} {} ({})",
                employee.getFirstName(), employee.getLastName(), employee.getEmail());
        Employee saved = employeeRepository.save(employee);
        log.info("[EMPLOYEE-SERVICE] Çalışan başarıyla kaydedildi -> id: {}", saved.getId());
        return saved;
    }

    public Employee update(Long id, Employee updatedEmployee) {
        log.info("[EMPLOYEE-SERVICE] Çalışan güncelleniyor -> id: {}", id);
        Employee existing = findById(id);

        existing.setFirstName(updatedEmployee.getFirstName());
        existing.setLastName(updatedEmployee.getLastName());
        existing.setEmail(updatedEmployee.getEmail());
        existing.setDepartment(updatedEmployee.getDepartment());
        existing.setPosition(updatedEmployee.getPosition());
        existing.setHireDate(updatedEmployee.getHireDate());

        Employee saved = employeeRepository.save(existing);
        log.info("[EMPLOYEE-SERVICE] Çalışan başarıyla güncellendi -> id: {}", saved.getId());
        return saved;
    }

    public void delete(Long id) {
        log.info("[EMPLOYEE-SERVICE] Çalışan siliniyor -> id: {}", id);
        Employee employee = findById(id);
        employeeRepository.delete(employee);
        log.info("[EMPLOYEE-SERVICE] Çalışan başarıyla silindi -> id: {}", id);
    }
}
