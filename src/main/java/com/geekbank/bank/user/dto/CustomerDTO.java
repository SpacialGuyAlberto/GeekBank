package com.geekbank.bank.user.dto;


public class CustomerDTO {

    private String firstName;
    private String lastName;
    private String email;
    private String password;

    public CustomerDTO(){

    }
    public CustomerDTO(String firstName, String email){
        this.firstName = firstName;
        this.email = email;
        this.password = password;
    }

    public String getFirstName() { return this.firstName; }
    public String getLastName() { return this.lastName; }
    public String getEmail() { return this.email; }
    public String getPassword() { return this.email; }

    public void setFirstName(String firstName){ this.firstName = firstName;}
    public void setPassword(String password){this.password = password; }
    @Override
    public String toString(){
        return  "UserDTO [firstName=" + firstName + ", email=" + email + ", password" + password;
    }
}
