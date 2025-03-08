package com.geekbank.bank.services;


import com.geekbank.bank.models.ActivationDetails;
import com.geekbank.bank.repositories.ActivationDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ActivationDetailsService {

    @Autowired
    private ActivationDetailsRepository activationDetailsRepository;

    /**
     * Crea o actualiza los detalles de activación para un kinguinId dado.
     */
    public ActivationDetails createOrUpdateActivationDetails(
            Long kinguinId,
            String videoUrl,
            String textDetails
    ) {

        Optional<ActivationDetails> optionalDetails = activationDetailsRepository.findByKinguinId(kinguinId);

        ActivationDetails details = optionalDetails.orElse(new ActivationDetails());
        details.setKinguinId(kinguinId);
        details.setVideoUrl(videoUrl);
        details.setTextDetails(textDetails);

        return activationDetailsRepository.save(details);
    }

    /**
     * Obtiene los detalles de activación para un kinguinId específico.
     */
    public Optional<ActivationDetails> getDetailsByKinguinId(Long kinguinId) {
        return activationDetailsRepository.findByKinguinId(kinguinId);
    }

    /**
     * Elimina los detalles de activación para un kinguinId específico.
     */
    public void deleteDetailsByKinguinId(Long kinguinId) {
        activationDetailsRepository.deleteByKinguinId(kinguinId);
    }
}


