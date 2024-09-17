package com.geekbank.bank.services;
import com.geekbank.bank.models.OrderRequest;
import com.geekbank.bank.models.Transaction;
import com.geekbank.bank.models.TransactionStatus;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Service
public class TransactionStorageService {
    private ConcurrentHashMap<String, Transaction> pendingTransactions = new ConcurrentHashMap<>();
    protected void storePendingTransaction(Transaction transaction){

        pendingTransactions.put(transaction.getPhoneNumber(), transaction);

        System.out.println("Stored transaction for phone number: " + transaction.getPhoneNumber() + "\n Transaction Id:  " + transaction.getTransactionNumber() );
    }

    public Transaction getTransactionByPhoneNumber(String phoneNumber){
        Transaction transaction = pendingTransactions.get(phoneNumber);
        System.out.println("Retrieved transaction for phone number: " + phoneNumber + " -> " + (transaction != null ? "Found" : "Not found"));
        return transaction;
    }

    public void removeTransaction(String phoneNumber){
        System.out.println("Attempting to remove the transaction in the queue associated to: " + phoneNumber);

        boolean removed = false;
        for (Map.Entry<String, Transaction> entry : pendingTransactions.entrySet()){
            if (entry.getKey().equals(phoneNumber.trim())){
                pendingTransactions.remove(entry.getKey());
                removed = true;
                break;
            }
        }

        System.out.println("Removal of Transaction: " + (removed ? "succeded" : "failed" + "for phone number: " +  phoneNumber));
    }

    public void removeTransactionById(Long transactionId){
        System.out.println("Remove transaction by Id");

        boolean removed = false;

    }

    public boolean hasTransactionForPhoneNumber(String phoneNumber){
        boolean isTransactionRemovedFromQueue = pendingTransactions.containsKey(phoneNumber);
        System.out.println("Checking if transaction exists for phone number in queue: " +  phoneNumber + " -> " + ( isTransactionRemovedFromQueue ? "Exists" : "Does not exist"));
        return isTransactionRemovedFromQueue;
    }

}

