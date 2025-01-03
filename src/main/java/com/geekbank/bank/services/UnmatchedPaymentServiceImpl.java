package com.geekbank.bank.services;

import com.geekbank.bank.models.UnmatchedPayment;
import com.geekbank.bank.repositories.UnmatchedPaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class UnmatchedPaymentServiceImpl implements UnmatchedPaymentService {

    @Autowired
    private UnmatchedPaymentRepository paymentRepository;

    @Override
    public UnmatchedPayment createUnmatchedPayment(UnmatchedPayment payment, MultipartFile image) throws IOException {
        if (image != null && !image.isEmpty()) {
            payment.setImage(image.getBytes());
        }
        // Obtener el usuario actual
        String currentUser = getCurrentAdminUsername();
        payment.setCreatedBy(currentUser);
        payment.setUpdatedBy(currentUser);
        return paymentRepository.save(payment);
    }

    @Override
    public UnmatchedPayment updateUnmatchedPayment(Long id, UnmatchedPayment paymentDetails, MultipartFile image) throws IOException {
        Optional<UnmatchedPayment> optionalPayment = paymentRepository.findById(id);
        if (!optionalPayment.isPresent()) {
            throw new RuntimeException("UnmatchedPayment not found with id " + id);
        }
        UnmatchedPayment payment = optionalPayment.get();
        payment.setPhoneNumber(paymentDetails.getPhoneNumber());
        payment.setAmountReceived(paymentDetails.getAmountReceived());
        payment.setReferenceNumber(paymentDetails.getReferenceNumber());
        payment.setReceivedAt(paymentDetails.getReceivedAt());
        payment.setConsumed(paymentDetails.isConsumed());
        payment.setDifferenceRedeemed(paymentDetails.isDifferenceRedeemed());
        payment.setVerified(paymentDetails.isVerified());
        payment.setSmsMessage(paymentDetails.getSmsMessage());

        if (image != null && !image.isEmpty()) {
            payment.setImage(image.getBytes());
        }

        // Actualizar el usuario que realizó la operación
        String currentUser = getCurrentAdminUsername();
        payment.setUpdatedBy(currentUser);

        return paymentRepository.save(payment);
    }

    @Override
    public UnmatchedPayment getUnmatchedPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UnmatchedPayment not found with id " + id));
    }

    @Override
    public List<UnmatchedPayment> getAllUnmatchedPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public void deleteUnmatchedPayment(Long id) {
        UnmatchedPayment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("UnmatchedPayment not found with id " + id));
        paymentRepository.delete(payment);
    }

    private String getCurrentAdminUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
}


