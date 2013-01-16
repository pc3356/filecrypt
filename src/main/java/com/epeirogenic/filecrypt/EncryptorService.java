package com.epeirogenic.filecrypt;

import org.apache.commons.io.FileUtils;
import org.jasypt.encryption.pbe.PBEByteCleanablePasswordEncryptor;

import java.io.File;

public class EncryptorService {

    private PBEByteCleanablePasswordEncryptor binaryEncryptor;

    private boolean initialised = false;

    public byte[] encrypt(File input, char[] password) throws Exception {

        if(!initialised) {
            binaryEncryptor.setPasswordCharArray(password);
            initialised = true;
        }

        byte[] fileAsBytes = FileUtils.readFileToByteArray(input);
        return binaryEncryptor.encrypt(fileAsBytes);
    }

    public void encrypt(File input, File output, char[] password) throws Exception {

        byte[] encryptedBinary = encrypt(input, password);
        FileUtils.writeByteArrayToFile(output, encryptedBinary);
    }

    public byte[] decrypt(File input, char[] password) throws Exception {

        if(!initialised) {
            binaryEncryptor.setPasswordCharArray(password);
            initialised = true;
        }

        byte[] fileAsBytes = FileUtils.readFileToByteArray(input);
        return binaryEncryptor.decrypt(fileAsBytes);
    }

    public void decrypt(File input, File output, char[] password) throws Exception {

        byte[] decryptedBinary = decrypt(input, password);
        FileUtils.writeByteArrayToFile(output, decryptedBinary);
    }

    public void setBinaryEncryptor(PBEByteCleanablePasswordEncryptor binaryEncryptor) {
        this.binaryEncryptor = binaryEncryptor;
    }
}
