package com.epeirogenic.filecrypt;

import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.*;
import org.jasypt.util.binary.StrongBinaryEncryptor;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
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
        options.addOption("a", "algorithm", true, "Algorithm to use (Default AES)");
        options.addOption("r", "recursive", false, "Recurse through subdirectories");
        options.addOption("o", "output", true, "Output path (default .)");
        options.addOption("e", "ext", true, "Output extension, default fcp");

        return options;
    }


    private File createOutputFile(File inputFile) throws Exception {

        //File path = new File(commandLine.getOptionValue("o", "."));
        File path = new File(FilenameUtils.getFullPath(inputFile.getPath()));
        String outputFileName = FilenameUtils.removeExtension(inputFile.getName());
        return new File(path, outputFileName);
    }

    public void decrypt() throws Exception {

        String password = commandLine.getOptionValue("p");
        String inputFile = commandLine.getOptionValue("i");
        boolean recurse = commandLine.hasOption("r");

        File file = new File(inputFile);
        if(file.isDirectory()) {
            decryptDirectory(file, password, recurse);
        } else {
            decryptFile(file, password);
        }

    }

    private void decryptDirectory(File directory, String password, boolean recurse) throws Exception {

        FileFilter fileFilter = FileFilterUtils.and(
            FileFileFilter.FILE,
            new SuffixFileFilter(".fcp")
        );

        File[] files = directory.listFiles(fileFilter);
        for(File file : files) {
            decryptFile(file, password);
        }

        if(recurse) {
            File[] subdirectories = directory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
            for(File subdirectory : subdirectories) {
                decryptDirectory(subdirectory, password, recurse);
            }
        }
    }

    private void decryptFile(File file, String password) throws Exception {
        byte[] fileAsBytes = FileUtils.readFileToByteArray(file);

        StrongBinaryEncryptor binaryEncryptor = new StrongBinaryEncryptor();
        binaryEncryptor.setPassword(password);

        byte[] decryptedBinary = binaryEncryptor.decrypt(fileAsBytes);

        // do something with this
        File outputFile = createOutputFile(file);
        FileUtils.writeByteArrayToFile(outputFile, decryptedBinary);
    }
}
