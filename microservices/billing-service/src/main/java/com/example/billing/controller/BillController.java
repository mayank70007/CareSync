package com.example.billing.controller;

import com.example.billing.model.Bill;
import com.example.billing.repository.BillRepository;
import org.springframework.http.*;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/bill")
public class BillController {
  private final BillRepository repo;
  private final RestTemplate restTemplate;
  public BillController(BillRepository repo, RestTemplate restTemplate){ this.repo = repo; this.restTemplate = restTemplate; }
    ")
  @PostMapping
  public ResponseEntity<Bill> create(@RequestParam Long patientId,
                                     @RequestParam Double amount,
                                     @RequestParam String billDate){
    Bill b = new Bill();
    b.setPatientId(patientId);
    b.setAmount(amount);
    b.setBillDate(LocalDate.parse(billDate));
    b.setStatus("PENDING");
    return ResponseEntity.ok(repo.save(b));
  }
    ")
  @GetMapping
  public List<Bill> all(Authentication auth, HttpServletRequest request){
    boolean isAdmin = auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));
    if(isAdmin) return repo.findAll();
    // patient: restrict to own bills
    Long pid = resolvePatientId(request);
    if(pid == null) return java.util.List.of();
    return repo.findByPatientId(pid);
  }
    ")
  @PostMapping("/{id}/pay")
  public ResponseEntity<Bill> pay(@PathVariable Long id, Authentication auth, HttpServletRequest request){
    boolean isAdmin = auth.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_ADMIN"));
    return repo.findById(id).map(b -> {
      if(!isAdmin){
        Long pid = resolvePatientId(request);
        if(pid == null || !pid.equals(b.getPatientId())) return ResponseEntity.status(403).<Bill>build();
      }
      b.setStatus("PAID");
      return ResponseEntity.ok(repo.save(b));
    }).orElse(ResponseEntity.notFound().build());
  }
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

