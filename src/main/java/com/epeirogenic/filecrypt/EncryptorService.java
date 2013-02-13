package com.epeirogenic.filecrypt;

import org.apache.commons.io.FileUtils;
import org.jasypt.encryption.pbe.PBEByteCleanablePasswordEncryptor;
import org.jasypt.util.binary.BinaryEncryptor;
import org.jasypt.util.binary.StrongBinaryEncryptor;

import java.io.File;

public class EncryptorService {

    public byte[] encrypt(File input, char[] password) throws Exception {

        StrongBinaryEncryptor binaryEncryptor = new StrongBinaryEncryptor();
        binaryEncryptor.setPasswordCharArray(password);

        byte[] fileAsBytes = FileUtils.readFileToByteArray(input);
        return binaryEncryptor.encrypt(fileAsBytes);
    }

    public void encrypt(File input, File output, char[] password) throws Exception {

        byte[] encryptedBinary = encrypt(input, password);
        FileUtils.writeByteArrayToFile(output, encryptedBinary);
    }

    public byte[] decrypt(File input, char[] password) throws Exception {

        StrongBinaryEncryptor binaryEncryptor = new StrongBinaryEncryptor();
        binaryEncryptor.setPasswordCharArray(password);

        byte[] fileAsBytes = FileUtils.readFileToByteArray(input);
        return binaryEncryptor.decrypt(fileAsBytes);
    }

    public void decrypt(File input, File output, char[] password) throws Exception {

        byte[] decryptedBinary = decrypt(input, password);
        FileUtils.writeByteArrayToFile(output, decryptedBinary);
    }
}
