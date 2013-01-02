package com.epeirogenic.filecrypt;

import org.apache.commons.io.FileUtils;
import org.jasypt.encryption.pbe.PBEByteEncryptor;

import java.io.File;

public class EncryptorService {

    private PBEByteEncryptor binaryEncryptor;

    public byte[] encrypt(File input, String password) throws Exception {

        byte[] fileAsBytes = FileUtils.readFileToByteArray(input);
        binaryEncryptor.setPassword(password);
        return binaryEncryptor.encrypt(fileAsBytes);
    }

    public void encrypt(File input, File output, String password) throws Exception {

        byte[] encryptedBinary = encrypt(input, password);
        FileUtils.writeByteArrayToFile(output, encryptedBinary);
    }

    public byte[] decrypt(File input, String password) throws Exception {

        byte[] fileAsBytes = FileUtils.readFileToByteArray(input);
        binaryEncryptor.setPassword(password);
        return binaryEncryptor.decrypt(fileAsBytes);
    }

    public void decrypt(File input, File output, String password) throws Exception {

        byte[] decryptedBinary = decrypt(input, password);
        FileUtils.writeByteArrayToFile(output, decryptedBinary);
    }

    public void setBinaryEncryptor(PBEByteEncryptor binaryEncryptor) {
        this.binaryEncryptor = binaryEncryptor;
    }
}
