package com.geekbank.bank.user.dto;

public class UserDTO {
    private Long id;
    private String email;
    private String name;
    private boolean enabled;

    public UserDTO() {
    }

    public UserDTO(Long id, String email, String name, boolean enabled) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
