package com.epeirogenic.filecrypt;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.jasypt.util.binary.StrongBinaryEncryptor;

public class EncryptorService {

    public byte[] encrypt(File input, char[] password) throws Exception {

        var binaryEncryptor = new StrongBinaryEncryptor();
        binaryEncryptor.setPasswordCharArray(password);

        var fileAsBytes = FileUtils.readFileToByteArray(input);
        return binaryEncryptor.encrypt(fileAsBytes);
    }

    public void encrypt(File input, File output, char[] password) throws Exception {

        var encryptedBinary = encrypt(input, password);
        FileUtils.writeByteArrayToFile(output, encryptedBinary);
    }

    public byte[] decrypt(File input, char[] password) throws Exception {

        var binaryEncryptor = new StrongBinaryEncryptor();
        binaryEncryptor.setPasswordCharArray(password);

        var fileAsBytes = FileUtils.readFileToByteArray(input);
        return binaryEncryptor.decrypt(fileAsBytes);
    }

    public void decrypt(File input, File output, char[] password) throws Exception {

        var decryptedBinary = decrypt(input, password);
        FileUtils.writeByteArrayToFile(output, decryptedBinary);
    }
}
