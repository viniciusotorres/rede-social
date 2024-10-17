package rede_social.rede_social.service.auth;

import java.security.SecureRandom;

public class CodeGenerator {

    private static final SecureRandom random = new SecureRandom();

    public static String generateCode(){
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
