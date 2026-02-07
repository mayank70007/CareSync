package com.example.medicalrecord.controller;

import com.example.medicalrecord.model.MedicalRecord;
import com.example.medicalrecord.repository.MedicalRecordRepository;
import org.springframework.http.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/medicalrecord")
public class MedicalRecordController {
  private final MedicalRecordRepository repo;
  private final RestTemplate restTemplate;
  public MedicalRecordController(MedicalRecordRepository repo, RestTemplate restTemplate){ this.repo = repo; this.restTemplate = restTemplate; }
    ")
  @PostMapping
  public ResponseEntity<MedicalRecord> create(@RequestParam Long patientId,
                                              @RequestParam String diagnosis,
                                              @RequestParam String treatment,
                                              @RequestParam String recordDate){
    MedicalRecord r = new MedicalRecord();
    r.setPatientId(patientId);
    r.setDiagnosis(diagnosis);
    r.setTreatment(treatment);
    r.setRecordDate(LocalDate.parse(recordDate));
    return ResponseEntity.ok(repo.save(r));
  }
    ")
  @GetMapping
  public List<MedicalRecord> all(Authentication auth, HttpServletRequest request){
    boolean isPatient = auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_PATIENT"));
    if(!isPatient) return repo.findAll();
    // patient: restrict to own records
    Long pid = resolvePatientId(request);
    if(pid == null) return java.util.List.of();
    return repo.findByPatientId(pid);
  }
  // Note: original monolith had no /count endpoint; omitted to keep parity.
    ")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id){
    repo.deleteById(id);
    return ResponseEntity.noContent().build();
  }

  private Long resolvePatientId(HttpServletRequest request){
    try{
      HttpHeaders headers = new HttpHeaders();
      String authz = request.getHeader("Authorization");
      if(authz!=null && !authz.isBlank()) headers.set("Authorization", authz);
      HttpEntity<Void> entity = new HttpEntity<>(headers);
      // GET /patient returns only the caller's own patient record when role is PATIENT
      var resp = restTemplate.exchange("http://localhost:8088/patient", HttpMethod.GET, entity, java.util.List.class);
      var list = (java.util.List<?>) resp.getBody();
      if(list==null || list.isEmpty()) return null;
      Object first = list.get(0);
      if(first instanceof java.util.Map<?,?> m){
        Object id = m.get("id");
        return id==null?null:Long.valueOf(id.toString());
      }
      return null;
    }catch(Exception e){ return null; }
  }
}

