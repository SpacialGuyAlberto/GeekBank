package com.geekbank.bank.models;

public enum Roles {
    ADMIN,
    CUSTOMER,
    SELLER,
    GUEST,
    MODERATOR,
    SUPPORT;

    @Override
    public String toString(){
        return "ROLE" + name();
    }
}
