package com.example.doctor.controller;

import com.example.doctor.dto.CreateDoctorRequest;
import com.example.doctor.model.Doctor;
import com.example.doctor.model.User;
import com.example.doctor.repository.DoctorRepository;
import com.example.doctor.repository.UserRepository;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;

@RestController
@RequestMapping("/doctor")
public class DoctorController {
    private final DoctorRepository repo;
    private final UserRepository users;
    private final RestTemplate restTemplate;
    public DoctorController(DoctorRepository repo, UserRepository users, RestTemplate restTemplate){ this.repo = repo; this.users = users; this.restTemplate = restTemplate; }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> addDoctor(@RequestBody CreateDoctorRequest req) {
        String name = req.getName();
        String specialization = req.getSpecialization();
        String phone = req.getPhone();
        String email = req.getEmail();
        if (name == null || name.trim().isEmpty()) return ResponseEntity.badRequest().body("Name is required and cannot be empty.");
        if (specialization == null || specialization.trim().isEmpty()) return ResponseEntity.badRequest().body("Specialization is required and cannot be empty.");
        if (phone == null || !phone.matches("\\d{10}")) return ResponseEntity.badRequest().body("Phone number must be exactly 10 digits.");
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) return ResponseEntity.badRequest().body("Email is required and must be valid.");
        if(req.getUsername()==null || req.getUsername().isBlank()) return ResponseEntity.badRequest().body("Username is required");
        if(req.getPassword()==null || req.getPassword().isBlank()) return ResponseEntity.badRequest().body("Password is required");
        try {
            // 1) create identity user via identity-service
            Map<String, String> payload = new HashMap<>();
            payload.put("username", req.getUsername());
            payload.put("password", req.getPassword());
            payload.put("role", "ROLE_DOCTOR");
            var identityResp = restTemplate.postForEntity("http://localhost:8085/register", payload, Map.class);
            if(!identityResp.getStatusCode().is2xxSuccessful()){
                return ResponseEntity.status(identityResp.getStatusCode()).body(identityResp.getBody());
            }
            // 2) create local doctor and link to local user row with same username
            User u = users.findByUsername(req.getUsername()).orElseGet(() -> {
                User nu = new User();
                nu.setUsername(req.getUsername());
                nu.setRole("ROLE_DOCTOR");
                return users.save(nu);
            });
            Doctor d = new Doctor();
            d.setName(name);
            d.setSpecialization(specialization);
            d.setPhone(phone);
            d.setEmail(email);
            d.setUser(u);
            return ResponseEntity.ok(repo.save(d));
        } catch(Exception e){ return ResponseEntity.status(500).body("Error saving doctor: "+e.getMessage()); }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctor) {
        return repo.findById(id).map(d -> {
            d.setName(doctor.getName());
            d.setSpecialization(doctor.getSpecialization());
            d.setPhone(doctor.getPhone());
            d.setEmail(doctor.getEmail());
            return ResponseEntity.ok(repo.save(d));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id){
        if(repo.existsById(id)){ repo.deleteById(id); return ResponseEntity.noContent().build(); }
        return ResponseEntity.notFound().build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<Doctor> getDoctorById(@PathVariable Long id){
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @GetMapping
    public List<Doctor> getAllDoctors(){ return repo.findAll(); }

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth){
        return repo.findByUserUsername(auth.getName()).<ResponseEntity<?>>map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    // Note: original monolith had no /count or /resolve-id endpoints; omitted to keep parity.

    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/my/patients")
    public List<?> myPatients(Authentication auth, HttpServletRequest request) {
        // Resolve current doctor id
        Long did = repo.findByUserUsername(auth.getName()).map(Doctor::getId).orElse(null);
        if(did == null) return List.of();
        // Forward Authorization to appointment service to list appointments by doctor
        HttpHeaders headers = new HttpHeaders();
        String authz = request.getHeader("Authorization");
        if(authz!=null && !authz.isBlank()) headers.set("Authorization", authz);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        try {
            var resp = restTemplate.exchange("http://localhost:8091/appointment", HttpMethod.GET, entity, java.util.List.class);
            java.util.List<?> list = resp.getBody();
            if(list == null) return List.of();
            // list contains maps with patientId; fetch unique patients from patient-service
            java.util.Set<Long> pids = new java.util.HashSet<>();
            for(Object o : list){
                if(o instanceof java.util.Map<?,?> m){
                    Object pid = m.get("patientId");
                    if(pid==null) pid = m.get("patient_id");
                    if(pid instanceof Number){ pids.add(((Number)pid).longValue()); }
                    else if(pid!=null){ try{ pids.add(Long.valueOf(pid.toString())); }catch(Exception ignored){} }
                }
            }
            java.util.List<java.util.Map<String,Object>> patients = new java.util.ArrayList<>();
            for(Long pid : pids){
                try{
                    var p = restTemplate.exchange("http://localhost:8088/patient/"+pid, HttpMethod.GET, entity, java.util.Map.class).getBody();
                    if(p!=null) patients.add((java.util.Map<String,Object>)p);
                }catch(Exception ignored){}
            }
            return patients;
        } catch(Exception e){
            return List.of();
        }
    }
}
