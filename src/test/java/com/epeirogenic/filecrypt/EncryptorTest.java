package com.epeirogenic.filecrypt;

import org.jasypt.encryption.pbe.config.SimplePBEConfig;
import org.junit.Test;

public class EncryptorTest {

    @Test
    public void check_available_methods() {

        SimplePBEConfig config = new SimplePBEConfig();
        config.setAlgorithm("MD5");
        config.setAlgorithm("SHA1");
        config.setAlgorithm("SHA-256");
        config.setAlgorithm("AES");
    }
}
