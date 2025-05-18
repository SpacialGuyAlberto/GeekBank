package com.geekbank.bank.payment.tigo.controller;

import com.geekbank.bank.payment.tigo.model.UnmatchedPayment;
import com.geekbank.bank.payment.tigo.repository.UnmatchedPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/admin/payments")
public class UnmatchedPaymentController {

    private final UnmatchedPaymentService paymentService;
    @Autowired
    public UnmatchedPaymentController(UnmatchedPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping(value = "/api/admin/payments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPayment(
            @RequestPart("payment") UnmatchedPayment unmatchedPayment,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            if (image != null && !image.isEmpty()) {
                Path path = Paths.get("uploads/" + image.getOriginalFilename());
                Files.write(path, image.getBytes());
                unmatchedPayment.setImagePath(path.toString());
            }

            return ResponseEntity.ok("Pago creado exitosamente.");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar el archivo: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<List<UnmatchedPayment>> getAllPayments() {
        List<UnmatchedPayment> payments = paymentService.getAllUnmatchedPayments();
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UnmatchedPayment> getPaymentById(@PathVariable Long id) {
        UnmatchedPayment payment = paymentService.getUnmatchedPaymentById(id);
        return new ResponseEntity<>(payment, HttpStatus.OK);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<UnmatchedPayment> createPayment(
            @RequestPart("payment") UnmatchedPayment payment,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            UnmatchedPayment createdPayment = paymentService.createUnmatchedPayment(payment, image);
            return new ResponseEntity<>(createdPayment, HttpStatus.CREATED);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<UnmatchedPayment> updatePayment(
            @PathVariable Long id,
            @RequestPart("payment") UnmatchedPayment paymentDetails,
            @RequestPart(value = "image", required = false) MultipartFile image) {
        try {
            UnmatchedPayment updatedPayment = paymentService.updateUnmatchedPayment(id, paymentDetails, image);
            return new ResponseEntity<>(updatedPayment, HttpStatus.OK);
        } catch (RuntimeException | IOException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deletePayment(@PathVariable Long id) {
        try {
            paymentService.deleteUnmatchedPayment(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<byte[]> getPaymentImage(@PathVariable Long id) {
        UnmatchedPayment payment = paymentService.getUnmatchedPaymentById(id);
        if (payment.getImage() != null) {
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"payment_" + id + ".jpg\"")
                    .body(payment.getImage());
        } else {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
    }
}

