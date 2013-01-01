package com.epeirogenic.filecrypt;

import com.epeirogenic.checksum.Checksum;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.jasypt.util.binary.StrongBinaryEncryptor;

import java.io.File;

public class Encryptor {

    private final static String DEFAULT_EXTENSION = "fcp";

	public static void main(String[] args) throws Exception {

        Encryptor encryptor = new Encryptor(args);
        encryptor.encrypt();
	}


    private CommandLine commandLine;

    public Encryptor(String[] args) throws Exception {

        commandLine = parseCommandLine(args);
    }

    private CommandLine parseCommandLine(String[] args) throws ParseException {

        CommandLineParser parser = new PosixParser();

        Options options = createOptions();
        return parser.parse(options, args, false);
    }

    private Options createOptions() {
        Options options = new Options();

        Option input = new Option("i", "input", true, "Path of input file/directory");
        input.setRequired(true);
        options.addOption(input);
        Option password = new Option("p", "pass", true, "Password");
        password.setRequired(true);
        options.addOption(password);
        options.addOption("a", "algorithm", true, "Algorithm to use (Default AES)");
        options.addOption("r", "recursive", false, "Recurse through subdirectories");
        options.addOption("o", "output", true, "Output path (default .)");
        options.addOption("e", "ext", true, "Output extension, default fcp");

        return options;
    }


    private File createOutputFile(File inputFile) throws Exception {

        Checksum checksum = Checksum.MD5;
        String checksumString = checksum.generateFor(inputFile);
        File path = new File(commandLine.getOptionValue("o", "."));
        return new File(path, checksumString + '.' + commandLine.getOptionValue("e", DEFAULT_EXTENSION));
    }

    public void encrypt() throws Exception {

        String password = commandLine.getOptionValue("p");
        String inputFile = commandLine.getOptionValue("i");

        File file = new File(inputFile);
        byte[] fileAsBytes = FileUtils.readFileToByteArray(file);

        StrongBinaryEncryptor binaryEncryptor = new StrongBinaryEncryptor();
        binaryEncryptor.setPassword(password);

        byte[] myEncryptedBinary = binaryEncryptor.encrypt(fileAsBytes);

        // do something with this
        File outputFile = createOutputFile(file);
        FileUtils.writeByteArrayToFile(outputFile, myEncryptedBinary);



        byte[] plainBinary = binaryEncryptor.decrypt(myEncryptedBinary);
    }
}
