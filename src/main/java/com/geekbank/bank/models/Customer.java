package com.geekbank.bank.models;
import jakarta.persistence.*;

@Entity
public class Customer extends User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    public Customer(String firstName, String email){
        this.firstName = firstName;
        this.email = email;
        this.password = password;
    }

    public Customer() {

    }

    public int getId() {
        return this.id;
    }
    public String getFirstName() { return this.firstName; }
    public String getLastName() { return this.lastName; }
    public String getEmail() { return this.email; }
    public String getPassword() { return this.email; }

    public void setFirstName(String firstName){ this.firstName = firstName;}
    public void setPassword(String password){this.password = password; }
}
