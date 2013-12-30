package com.epeirogenic.filecrypt;

import com.epeirogenic.checksum.Checksum;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileFilter;

public class FileCryptService {

    private final static Logger LOGGER = Logger.getLogger(FileCryptService.class);

    private final static String DEFAULT_EXTENSION = "fcf";

    private EncryptorService encryptorService;

    public FileCryptService() {
        this(new EncryptorService());
    }

    public FileCryptService(EncryptorService encryptorService) {
        this.encryptorService = encryptorService;
    }

    public void encrypt(String passwordString, File inputFile, File outputFile) {

        char[] password = passwordString.toCharArray();
        if (inputFile.isDirectory()) {
            iterate(inputFile, password, false, outputFile);
        } else {
            encryptFile(inputFile, password, outputFile);
        }
        LOGGER.info("Encryption complete");
    }

    private void encryptFile(File input, char[] password, File output) {
        performCryptOperation(input, password, false, output);
    }

    public void decrypt(String passwordString, File inputFile, File outputFile) {

        char[] password = passwordString.toCharArray();
        if (inputFile.isDirectory()) {
            System.out.println(inputFile.getAbsolutePath() + " is a directory");
            iterate(inputFile, password, true, outputFile);
        } else {
            System.out.println(inputFile.getAbsolutePath() + " is a file");
            decryptFile(inputFile, password, outputFile);
        }
        LOGGER.info("Decryption complete");
    }

    private void decryptFile(File input, char[] password, File outputFile) {
        performCryptOperation(input, password, true, outputFile);
    }

    private void performCryptOperation(File input, char[] password, boolean decrypt, File outputFile) {
        LOGGER.debug(password == null ? "PASSWORD IS NULL" : "PASSWORD OK");
        try {
            if (decrypt) {
                File output = createDecryptOutputFile(input, outputFile);
                LOGGER.debug("Decrypting " + input.getName());
                LOGGER.debug("To: " + output.getName());
                encryptorService.decrypt(input, output, password);
                LOGGER.debug("Decrypted: " + input.getAbsolutePath() + " to: " + output.getAbsolutePath());
            } else {
                File output = createEncryptOutputFile(input, outputFile);
                LOGGER.debug("Encrypting " + input.getName());
                LOGGER.debug("To: " + output.getName());
                encryptorService.encrypt(input, output, password);
                LOGGER.debug("Encrypted: " + input.getAbsolutePath() + " to: " + output.getAbsolutePath());
            }
        } catch (Exception e) {
            LOGGER.error("Error encrypting file " + input.getName() +
                    (e.getMessage() == null ? "" : " : " + e.getMessage()), e);
        }
    }

    private File createEncryptOutputFile(File inputFile, File outputFile) {

        if (outputFile.isDirectory()) {
            return createChecksumFilename(inputFile, outputFile);
        }
        return outputFile;
    }

    public File createDecryptOutputFile(File inputFile, File outputFile) {

        if (outputFile.isDirectory()) {
            String outputFileName = FilenameUtils.removeExtension(inputFile.getName());
            return new File(outputFile, outputFileName);
        } else {
            return outputFile;
        }
    }

    private File createChecksumFilename(File inputFile, File outputFile) {

        try {
            Checksum checksum = Checksum.MD5;
            // trying to cteate a checksum of the directory, rather than the individual file
            String checksumString = checksum.generateFor(inputFile);
            String extension = FilenameUtils.getExtension(inputFile.getName());

            LOGGER.debug("Checksum: " + checksumString);
            LOGGER.debug("Extension: " + extension);

            File path;
            if (outputFile.isDirectory()) {
                path = outputFile;
            } else {
                path = outputFile.getParentFile();
            }
            String fileName = checksumString + '.' + extension + '.' + DEFAULT_EXTENSION;
            LOGGER.debug("Filename: " + fileName);

            File output = new File(path, fileName);

            LOGGER.debug("Output file (checksum name): " + output.getAbsolutePath());

            return output;

        } catch (Exception e) {
            LOGGER.error("Unable to create output file", e);
            return null;
        }
    }

    private void iterate(File input, char[] password, boolean decrypt, File outputFile) {

        if (input.isDirectory()) {
            FileFilter fileFilter = FileFilterUtils.and(FileFileFilter.FILE, HiddenFileFilter.VISIBLE);
            File[] files = input.listFiles(fileFilter);

            for (File file : files) {
                System.out.println((decrypt ? "De" : "En") + "crypting " + file.getName());
                performCryptOperation(file, password, decrypt, outputFile);
            }
        }
    }
}
