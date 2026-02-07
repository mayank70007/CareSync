package com.example.patient.controller;

import com.example.patient.dto.CreatePatientRequest;
import com.example.patient.model.Patient;
import com.example.patient.model.User;
import com.example.patient.repository.PatientRepository;
import com.example.patient.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/patient")
public class PatientController {
    private final PatientRepository repo;
    private final UserRepository users;
    private final RestTemplate rest;
    public PatientController(PatientRepository repo, UserRepository users, RestTemplate rest){ this.repo = repo; this.users = users; this.rest = rest; }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    @PostMapping
    public ResponseEntity<?> add(@RequestBody CreatePatientRequest req){
        String phone = req.getPhone(); String name = req.getName(); String gender = req.getGender(); String address = req.getAddress(); int age = req.getAge();
        if (name == null || name.trim().isEmpty()) return ResponseEntity.badRequest().body("Name is required and cannot be empty.");
        if (gender == null || gender.trim().isEmpty()) return ResponseEntity.badRequest().body("Gender is required and cannot be empty.");
        if (address == null || address.trim().isEmpty()) return ResponseEntity.badRequest().body("Address is required and cannot be empty.");
        if (phone == null || !phone.matches("\\d{10}")) return ResponseEntity.badRequest().body("Phone number must be exactly 10 digits.");
        if (age <= 0) return ResponseEntity.badRequest().body("Age must be a positive number.");
        if(req.getUsername()==null || req.getUsername().isBlank()) return ResponseEntity.badRequest().body("Username is required");
        if(req.getPassword()==null || req.getPassword().isBlank()) return ResponseEntity.badRequest().body("Password is required");
        try {
            // 1) register user in identity-service
            Map<String,String> body = new HashMap<>();
            body.put("username", req.getUsername());
            body.put("password", req.getPassword());
            body.put("role", "ROLE_PATIENT");
            var resp = rest.postForEntity("http://localhost:8085/register", body, Map.class);
            if(!resp.getStatusCode().is2xxSuccessful()) return ResponseEntity.status(resp.getStatusCode()).body(resp.getBody());
            // 2) create local user record if absent
            User u = users.findByUsername(req.getUsername()).orElseGet(() -> {
                User nu = new User();
                nu.setUsername(req.getUsername());
                nu.setRole("ROLE_PATIENT");
                return users.save(nu);
            });
            Patient p = new Patient();
            p.setName(name); p.setGender(gender); p.setAddress(address); p.setPhone(phone); p.setAge(age);
            p.setUser(u);
            return ResponseEntity.ok(repo.save(p));
        } catch(Exception e){ return ResponseEntity.status(500).body("Error saving patient: "+e.getMessage()); }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id){ if(repo.existsById(id)){ repo.deleteById(id); return ResponseEntity.noContent().build(); } return ResponseEntity.notFound().build(); }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT')")
    @GetMapping("/{id}")
    public ResponseEntity<?> byId(@PathVariable Long id, Authentication auth){
        var opt = repo.findById(id); if(opt.isEmpty()) return ResponseEntity.notFound().build();
        boolean isPatient = auth.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_PATIENT"));
        if(isPatient){ String username = auth.getName(); Long pid = repo.findByUserUsername(username).map(Patient::getId).orElse(null); if(pid==null || !pid.equals(id)) return ResponseEntity.status(403).body("Forbidden"); }
        return ResponseEntity.ok(opt.get());
    }

    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT')")
    @GetMapping
    public List<Patient> list(Authentication auth,
                              @RequestParam(required=false) String name,
                              @RequestParam(required=false) Integer age,
                              @RequestParam(required=false) String gender){
        boolean isPatient = auth.getAuthorities().stream().anyMatch(ga -> ga.getAuthority().equals("ROLE_PATIENT"));
        if(isPatient){
            // For patients, only return their own record to support self-scoped cross-service calls
            Long pid = repo.findByUserUsername(auth.getName()).map(Patient::getId).orElse(null);
            if(pid == null) return java.util.List.of();
            return repo.findById(pid).map(java.util.List::of).orElse(java.util.List.of());
        }
        if(name!=null) return repo.findByNameContainingIgnoreCase(name);
        if(age!=null) return repo.findByAge(age);
        if(gender!=null) return repo.findByGenderIgnoreCase(gender);
        return repo.findAll();
    }

    // Note: original monolith had no /resolve-id or /count endpoints; omitting extras to keep parity.
}
