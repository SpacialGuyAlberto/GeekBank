package com.geekbank.bank.services;

import com.geekbank.bank.models.OrderRequest;
import com.geekbank.bank.models.Promotion;
import com.geekbank.bank.models.User;
import com.geekbank.bank.repositories.PromotionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class PromoService {

    @Autowired
    private PromotionRepository promotionRepository;

    private Promotion createPromo(User user,  double discount){
        Promotion promo = new Promotion();
        promo.setCode(generatePromoCode());
        promo.setDiscountPorcentage(discount);
        promotionRepository.save(promo);

        return promo;
    }

    private String generatePromoCode(){
        String alphabet = "ABCDFGHIJKLMNOPQRSTUVWXYZ";
        Random random = new Random();
        String Prefix = "PROMO";
        int randomPredicateFirst = random.nextInt(99);
        StringBuilder randomPredicateSecond = new StringBuilder();
        int randomSuffix = random.nextInt(9999);
        String code = "";

        for (int i = 0; i < 2; i++){
            randomPredicateSecond.append(alphabet.charAt(random.nextInt(alphabet.length())));
        }

        code = Prefix + randomPredicateFirst + randomPredicateSecond + randomSuffix;

        return code;
    }

   public Boolean checkIfExists(String code){

        if (!promotionRepository.findByCode(code).isEmpty()){
            return true;
        }

        return false;
    }

    public Promotion getPromoByCode(String code){
        return promotionRepository.findByCode(code).orElse(null);
    }
}
