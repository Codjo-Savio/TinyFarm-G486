package com.api.tinyfarm.controller;

import com.api.tinyfarm.model.Transaction;
import com.api.tinyfarm.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // GET

    @GetMapping("/id/{id}")
    @PreAuthorize("@securityAuthorizationService.canAccessTransaction(authentication, #id)")
    public ResponseEntity<Transaction> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(transactionService.findById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/buyer/{buyer}")
    @PreAuthorize("@securityAuthorizationService.canAccessUser(authentication, #buyer)")
    public ResponseEntity<Transaction> getByBuyer(@PathVariable Long buyer) {
        try {
            return ResponseEntity.ok(transactionService.findByBuyer(buyer));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/seller/{seller}")
    @PreAuthorize("@securityAuthorizationService.canAccessUser(authentication, #seller)")
    public ResponseEntity<Transaction> getBySeller(@PathVariable Long seller) {
        try {
            return ResponseEntity.ok(transactionService.findBySeller(seller));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/product/{product}")
    public ResponseEntity<Transaction> getByProduct(
        @PathVariable Long product
    ) {
        try {
            return ResponseEntity.ok(transactionService.findByProduct(product));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST

    @PostMapping("")
    @PreAuthorize("@securityAuthorizationService.canSubmitTransaction(authentication, #transaction)")
    public ResponseEntity<Transaction> create(
        @RequestBody Transaction transaction
    ) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(
                transactionService.create(transaction)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            return ResponseEntity.status(
                HttpStatus.INTERNAL_SERVER_ERROR
            ).build();
        }
    }

    // PUT

    @PutMapping("/id/{id}")
    @PreAuthorize("@securityAuthorizationService.canAccessTransaction(authentication, #id)")
    public ResponseEntity<Transaction> update(
        @PathVariable Long id,
        @RequestBody Transaction transaction
    ) {
        try {
            return ResponseEntity.ok(
                transactionService.update(id, transaction)
            );
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE

    @DeleteMapping("/id/{id}")
    @PreAuthorize("@securityAuthorizationService.canAccessTransaction(authentication, #id)")
    public ResponseEntity<Void> deleteById(@PathVariable Long id) {
        try {
            transactionService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
