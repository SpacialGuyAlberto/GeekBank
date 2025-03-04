package com.geekbank.bank.config;

import io.github.cdimascio.dotenv.Dotenv;

public class ConfigLoader {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory("./")
            .ignoreIfMissing()
            .load();
    public static String getVariable(String key) {
        return dotenv.get(key);
    }

}
