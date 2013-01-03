package com.epeirogenic.filecrypt;

import com.epeirogenic.checksum.Checksum;
import org.apache.commons.cli.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.jasypt.util.binary.StrongBinaryEncryptor;

import java.io.File;
import java.io.FileFilter;

public class Encryptor {

    private final static String DEFAULT_EXTENSION = "fcp";

	public static void main(String[] args) throws Exception {

        Encryptor encryptor = new Encryptor(args);
        encryptor.encrypt();
	}

    private CommandLine commandLine;
    private Options options;

    public Encryptor(String[] args) throws Exception {

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

        Checksum checksum = Checksum.MD5;
        String checksumString = checksum.generateFor(inputFile);
        String extension = FilenameUtils.getExtension(inputFile.getName());
        //File path = new File(commandLine.getOptionValue("o", "."));
        File path = new File(FilenameUtils.getFullPath(inputFile.getPath()));
        String fileName = checksumString + '.' + extension + '.' + commandLine.getOptionValue("e", DEFAULT_EXTENSION);
        return new File(path, fileName);
    }

    public void encrypt() throws Exception {

        String password = commandLine.getOptionValue("p");
        String inputFile = commandLine.getOptionValue("i");
        boolean recurse = commandLine.hasOption("r");

        File file = new File(inputFile);
        if(file.isDirectory()) {
            encryptDirectory(file, password, recurse);
        } else {
            encryptFile(file, password);
        }
    }

    private void encryptDirectory(File directory, String password, boolean recurse) throws Exception {

        FileFilter fileFilter = FileFilterUtils.and(FileFileFilter.FILE);
        File[] files = directory.listFiles(fileFilter);
        for(File file : files) {
            encryptFile(file, password);
        }

        if(recurse) {
            File[] subdirectories = directory.listFiles((FileFilter) DirectoryFileFilter.DIRECTORY);
            for(File subdirectory : subdirectories) {
                encryptDirectory(subdirectory, password, recurse);
            }
        }
    }

    private void encryptFile(File file, String password) throws Exception {
        byte[] fileAsBytes = FileUtils.readFileToByteArray(file);

        StrongBinaryEncryptor binaryEncryptor = new StrongBinaryEncryptor();
        binaryEncryptor.setPassword(password);

        byte[] encryptedBinary = binaryEncryptor.encrypt(fileAsBytes);

        // do something with this
        File outputFile = createOutputFile(file);
        FileUtils.writeByteArrayToFile(outputFile, encryptedBinary);
    }
}
