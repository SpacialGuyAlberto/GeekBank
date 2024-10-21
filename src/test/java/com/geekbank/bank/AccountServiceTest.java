package com.geekbank.bank;

import com.geekbank.bank.models.Account;
import com.geekbank.bank.models.AccountStatus;
import com.geekbank.bank.models.VerificationStatus;
import com.geekbank.bank.repositories.AccountRepository;
import com.geekbank.bank.services.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateAccount() {
        Account account = new Account();
        account.setUser(null); // Asumiendo que el usuario se establece en otro lugar

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account savedAccount = invocation.getArgument(0);
            savedAccount.setId(1L);
            return savedAccount;
        });

        Account result = accountService.createAccount(account);

        assertNotNull(result);
        assertEquals(AccountStatus.ACTIVE, result.getStatus());
        assertEquals(VerificationStatus.UNVERIFIED, result.getVerificationStatus());
        assertEquals(0.0, result.getBalance());
        assertEquals(0, result.getLoyaltyPoints());
        assertNotNull(result.getAccountNumber());

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    public void testGetAccountById() {
        Account account = new Account();
        account.setId(1L);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        Optional<Account> result = accountService.getAccountById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());

        verify(accountRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetAccountByAccountNumber() {
        Account account = new Account();
        account.setAccountNumber("ACC123");

        when(accountRepository.findByAccountNumber("ACC123")).thenReturn(Optional.of(account));

        Optional<Optional<Account>> result = accountService.getAccountByAccountNumber("ACC123");

        assertTrue(result.isPresent());
        assertTrue(result.get().isPresent());
        assertEquals("ACC123", result.get().get().getAccountNumber());

        verify(accountRepository, times(1)).findByAccountNumber("ACC123");
    }


    @Test
    public void testUpdateAccountExists() {
        Account account = new Account();
        account.setId(1L);

        when(accountRepository.existsById(1L)).thenReturn(true);
        when(accountRepository.save(account)).thenReturn(account);

        Account result = accountService.updateAccount(account);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(accountRepository, times(1)).existsById(1L);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    public void testUpdateAccountDoesNotExist() {
        Account account = new Account();
        account.setId(1L);

        when(accountRepository.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            accountService.updateAccount(account);
        });

        verify(accountRepository, times(1)).existsById(1L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testDeleteAccountExists() {
        when(accountRepository.existsById(1L)).thenReturn(true);

        accountService.deleteAccount(1L);

        verify(accountRepository, times(1)).existsById(1L);
        verify(accountRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteAccountDoesNotExist() {
        when(accountRepository.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> {
            accountService.deleteAccount(1L);
        });

        verify(accountRepository, times(1)).existsById(1L);
        verify(accountRepository, never()).deleteById(anyLong());
    }

    @Test
    public void testAddLoyaltyPoints() {
        Account account = new Account();
        account.setId(1L);
        account.setLoyaltyPoints(10);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        accountService.addLoyaltyPoints(1L, 5);

        assertEquals(15, account.getLoyaltyPoints());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    public void testDebitAccountSufficientFunds() {
        Account account = new Account();
        account.setId(1L);
        account.setBalance(100.0);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        accountService.debitAccount(1L, 50.0);

        assertEquals(50.0, account.getBalance());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    public void testDebitAccountInsufficientFunds() {
        Account account = new Account();
        account.setId(1L);
        account.setBalance(30.0);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(RuntimeException.class, () -> {
            accountService.debitAccount(1L, 50.0);
        });

        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    public void testCreditAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setBalance(100.0);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        accountService.creditAccount(1L, 50.0);

        assertEquals(150.0, account.getBalance());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    public void testChangeAccountStatus() {
        Account account = new Account();
        account.setId(1L);
        account.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        accountService.changeAccountStatus(1L, AccountStatus.SUSPENDED);

        assertEquals(AccountStatus.SUSPENDED, account.getStatus());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    public void testChangeVerificationStatus() {
        Account account = new Account();
        account.setId(1L);
        account.setVerificationStatus(VerificationStatus.UNVERIFIED);

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);

        accountService.changeVerificationStatus(1L, VerificationStatus.VERIFIED);

        assertEquals(VerificationStatus.VERIFIED, account.getVerificationStatus());
        verify(accountRepository, times(1)).findById(1L);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    public void testGetAllAccounts() {
        Account account1 = new Account();
        account1.setId(1L);

        Account account2 = new Account();
        account2.setId(2L);

        List<Account> accounts = Arrays.asList(account1, account2);
        Page<Account> page = new PageImpl<>(accounts);

        when(accountRepository.findAll(any(Pageable.class))).thenReturn(page);

        Page<Account> result = accountService.getAllAccounts(PageRequest.of(0, 10));

        assertEquals(2, result.getTotalElements());
        verify(accountRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    public void testGetAccountsByStatus() {
        Account account1 = new Account();
        account1.setId(1L);
        account1.setStatus(AccountStatus.ACTIVE);

        Account account2 = new Account();
        account2.setId(2L);
        account2.setStatus(AccountStatus.ACTIVE);

        List<Account> accounts = Arrays.asList(account1, account2);

        when(accountRepository.findByStatus(AccountStatus.ACTIVE)).thenReturn(accounts);

        List<Account> result = accountService.getAccountsByStatus(AccountStatus.ACTIVE);

        assertEquals(2, result.size());
        verify(accountRepository, times(1)).findByStatus(AccountStatus.ACTIVE);
    }
}
