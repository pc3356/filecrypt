package com.epeirogenic.filecrypt;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.jasypt.util.binary.StrongBinaryEncryptor;

import java.io.File;
import java.util.Collection;

public class Decryptor {

    public static void main(String[] args) throws Exception {

        Decryptor decryptor = new Decryptor(args);
        decryptor.decrypt();
    }

    private Options options;
    private CommandLine commandLine;

    public Decryptor(String[] args) throws Exception {

        options = createOptions();
        try {
            commandLine = parseCommandLine(args);
        } catch(ParseException pe) {
            System.err.println("Options:");
            for(Object o : options.getOptions()) {
                Option option = (Option)o;
                System.err.println(option.toString());
            }
        }
    }

    private CommandLine parseCommandLine(String[] args) throws ParseException {

        CommandLineParser parser = new PosixParser();
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
        Option outputFileName = new Option("n", "name", true, "Output file name");
        outputFileName.setRequired(true);
        options.addOption(outputFileName);
        options.addOption("a", "algorithm", true, "Algorithm to use (Default AES)");
        options.addOption("r", "recursive", false, "Recurse through subdirectories");
        options.addOption("o", "output", true, "Output path (default .)");
        options.addOption("e", "ext", true, "Output extension, default fcp");

        return options;
    }


    private File createOutputFile() throws Exception {

        File path = new File(commandLine.getOptionValue("o", "."));
        return new File(path, commandLine.getOptionValue("n"));
    }

    public void decrypt() throws Exception {

        String password = commandLine.getOptionValue("p");
        String inputFile = commandLine.getOptionValue("i");

        File file = new File(inputFile);
        byte[] fileAsBytes = FileUtils.readFileToByteArray(file);

        StrongBinaryEncryptor binaryEncryptor = new StrongBinaryEncryptor();
        binaryEncryptor.setPassword(password);

        byte[] decryptedBinary = binaryEncryptor.decrypt(fileAsBytes);

        // do something with this
        File outputFile = createOutputFile();
        FileUtils.writeByteArrayToFile(outputFile, decryptedBinary);

    }
}
