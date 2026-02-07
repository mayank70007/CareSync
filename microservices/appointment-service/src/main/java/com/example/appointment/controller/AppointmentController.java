package com.example.appointment.controller;

import com.example.appointment.model.Appointment;
import com.example.appointment.repository.AppointmentRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/appointment")
public class AppointmentController {
    private final AppointmentRepository repo;
    private final RestTemplate restTemplate;
    
    public AppointmentController(AppointmentRepository repo, RestTemplate restTemplate) {
        this.repo = repo; 
        this.restTemplate = restTemplate;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestParam Long patientId,
                                  @RequestParam Long doctorId,
                                  @RequestParam LocalDateTime appointmentTime,
                                  HttpServletRequest request) {
        String userRole = request.getHeader("X-User-Role");
        if (!"ROLE_ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }
        
        Appointment a = new Appointment();
        a.setPatientId(patientId);
        a.setDoctorId(doctorId);
        a.setAppointmentTime(appointmentTime);
        a.setStatus("SCHEDULED");
        return ResponseEntity.ok(repo.save(a));
    }

    @GetMapping
    public ResponseEntity<?> list(HttpServletRequest request) {
        String userRole = request.getHeader("X-User-Role");
        
        if (!"ROLE_ADMIN".equals(userRole) && !"ROLE_DOCTOR".equals(userRole) && !"ROLE_PATIENT".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied");
        }
        
        if ("ROLE_ADMIN".equals(userRole)) {
            return ResponseEntity.ok(repo.findAll());
        }
        
        HttpHeaders headers = new HttpHeaders();
        String authz = request.getHeader("Authorization");
        if (authz != null && !authz.isBlank()) headers.set("Authorization", authz);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        
        if ("ROLE_DOCTOR".equals(userRole)) {
            try {
                var resp = restTemplate.exchange("http://localhost:8086/doctor/me", HttpMethod.GET, entity, java.util.Map.class);
                Object id = resp.getBody() == null ? null : ((java.util.Map<?,?>)resp.getBody()).get("id");
                if (id != null) { 
                    return ResponseEntity.ok(repo.findByDoctorId(Long.valueOf(id.toString()))); 
                }
            } catch (Exception ignored) {}
            return ResponseEntity.ok(java.util.List.of());
        } else {
            try {
                var resp = restTemplate.exchange("http://localhost:8088/patient", HttpMethod.GET, entity, java.util.List.class);
                var list = (java.util.List<?>) resp.getBody();
                if (list != null && !list.isEmpty()) {
                    Object first = list.get(0);
                    if (first instanceof java.util.Map<?,?> m) {
                        Object id = m.get("id");
                        if (id != null) { 
                            return ResponseEntity.ok(repo.findByPatientId(Long.valueOf(id.toString()))); 
                        }
                    }
                }
            } catch (Exception ignored) {}
            return ResponseEntity.ok(java.util.List.of());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        String userRole = request.getHeader("X-User-Role");
        if (!"ROLE_ADMIN".equals(userRole)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if (repo.existsById(id)) { 
            repo.deleteById(id); 
            return ResponseEntity.noContent().build(); 
        }
        return ResponseEntity.notFound().build();
    }
}
