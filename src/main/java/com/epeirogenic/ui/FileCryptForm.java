package com.epeirogenic.ui;

import com.epeirogenic.checksum.Checksum;
import com.epeirogenic.filecrypt.EncryptorService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.log4j.Logger;
import org.jasypt.encryption.pbe.StandardPBEByteEncryptor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileFilter;
import java.util.List;

public class FileCryptForm extends JDialog {

    private static final Logger LOGGER = Logger.getLogger(FileCryptForm.class);

    private Worker worker;

    private File inputFile;

    private File outputFile;
    private final static String DEFAULT_EXTENSION = "fcf";

    private EncryptorService encryptorService;

	public FileCryptForm(EncryptorService encryptorService) {

        this.encryptorService = encryptorService;
        createUIComponents();
	}

	private void createUIComponents() {

        setContentPane(panel);
        setModal(true);
        //getRootPane().setDefaultButton(browseInputButton);
        setTitle("FileCrypt");
        setResizable(false);
        setLocation(new Point(500, 300));

        browseInputButton.setEnabled(true);
        browseInputButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        chooseInputFile();
                    }
                }
        );

        browseOutputButton.setEnabled(true);
        browseOutputButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        chooseOutputFile();
                    }
                }
        );

        encryptButton.setEnabled(false);
        encryptButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        encrypt();
                    }
                }
        );

        decryptButton.setEnabled(false);
        decryptButton.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        decrypt();
                    }
                }
        );

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if(e != null) {
                    onCancel();
                }
            }
        });

        // call onCancel() on ESCAPE
        panel.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(e != null) {
                    onCancel();
                }
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        worker = new Worker();

        this.pack();
        this.setVisible(true);

        LOGGER.info("Created UI components");
	}

    private void encrypt() {

        char[] password = prepareForOperation();
        encryptorService.setBinaryEncryptor(new StandardPBEByteEncryptor());
        if(inputFile.isDirectory()) {
            iterate(inputFile, password, false);
        } else {
            encryptFile(inputFile, password);
        }
    }

    private void encryptFile(File input, char[] password) {
        performCryptOperation(input, password, false);
    }

    private void decrypt() {

        char[] password = prepareForOperation();
        encryptorService.setBinaryEncryptor(new StandardPBEByteEncryptor());
        if(inputFile.isDirectory()) {
            iterate(inputFile, password, true);
        } else {
            decryptFile(inputFile, password);
        }
    }

    private void decryptFile(File input, char[] password) {
        performCryptOperation(input, password, true);
    }

    private void performCryptOperation(File input, char[] password, boolean decrypt) {
        LOGGER.info(password == null ? "PASSWORD IS NULL" : "PASSWORD OK");
        try {
            if(decrypt) {
                File output = createDecryptOutputFile(outputFile);
                LOGGER.info("Decrypting " + input.getName());
                LOGGER.info("To: " + output.getName());
                encryptorService.decrypt(input, output, password);
            } else {
                File output = createEncryptOutputFile(outputFile);
                LOGGER.info("Encrypting " + input.getName());
                LOGGER.info("To: " + output.getName());
                encryptorService.encrypt(input, output, password);
            }
        } catch(Exception e) {
            LOGGER.error("Error encrypting file " + input.getName() +
                    (e.getMessage() == null ? "" : " : " + e.getMessage()), e);
        }
    }

    private File createEncryptOutputFile(File inputFile) {

        if(outputFile.isFile()) {
            return outputFile;
        } else {
            return createChecksumFilename(inputFile);
        }
    }

    public File createDecryptOutputFile(File inputFile) {
        File path = inputFile.isDirectory() ? inputFile : inputFile.getParentFile();
        String outputFileName = FilenameUtils.removeExtension(inputFile.getName());
        return new File(path, outputFileName);
    }

    private File createChecksumFilename(File inputFile) {

        try {
            Checksum checksum = Checksum.MD5;
            String checksumString = checksum.generateFor(inputFile);
            String extension = FilenameUtils.getExtension(inputFile.getName());

            File path;
            if(outputFile.isDirectory()) {
                path = outputFile;
            } else {
                path = outputFile.getParentFile();
            }
            String fileName = checksumString + '.' + extension + '.' + DEFAULT_EXTENSION;
            return new File(path, fileName);
        } catch(Exception e) {
            LOGGER.error("Unable to create output file", e);
            return null;
        }
    }

    private char[] prepareForOperation() {

        if(inputFile == null) {
            throw new IllegalArgumentException("No input file provided");
        }

        if(outputFile == null) {
            // set the output field + file to show input directory
            outputFile = inputFile.getParentFile();
            outputFileField.setText(outputFile.getAbsolutePath());
        }

        return passwordField.getPassword();
    }

    private void iterate(File input, char[] password, boolean decrypt) {

        if(input.isDirectory()) {
            FileFilter fileFilter = FileFilterUtils.and(FileFileFilter.FILE, HiddenFileFilter.VISIBLE);
            File[] files = input.listFiles(fileFilter);

            for(File file : files) {
                performCryptOperation(file, password, decrypt);
            }

        }
    }

    private void onCancel() {

        worker.cancel(true);
        dispose();
    }

    private void chooseInputFile() {
        inputFile = chooseFile("Input", inputFile);
        if(inputFile != null) {
            inputFileField.setText(inputFile.getAbsolutePath());
            encryptButton.setEnabled(true);
            decryptButton.setEnabled(true);
        }
    }

    private void chooseOutputFile() {
        outputFile = chooseFile("Output", outputFile);
        if(outputFile != null) {
            outputFileField.setText(outputFile.getAbsolutePath());
        }
    }

    private File chooseFile(String title, File start) {
        LOGGER.debug("Open file dialog");

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose " + title + " file");

        fileChooser.setCurrentDirectory(determineStartDirectory(start));

        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            LOGGER.debug("getCurrentDirectory(): " + fileChooser.getCurrentDirectory());
            LOGGER.debug("getSelectedFile() : " + fileChooser.getSelectedFile());

            return fileChooser.getSelectedFile();

        } else {
            LOGGER.debug("No file selection ");
            return null;
        }
    }

    private File determineStartDirectory(File file) {

        if(file == null) {
            return new File(System.getProperty("user.home", "/"));
        } else {
            if(file.isDirectory()) {
                return file;
            } else {
                return file.getParentFile();
            }
        }
    }

    /* =============================================== */

    class Worker extends SwingWorker<Void, String> {

        @Override
        protected Void doInBackground() throws Exception {

            return null;
        }

        @Override
        protected void process(List<String> messages) {

        }

        public void publish(String message) {
            super.publish(message);
        }

        @Override
        protected void done() {
            super.done();
        }

    }
    public static void main(String[] args) {

        EncryptorService encryptorService = new EncryptorService();
        new FileCryptForm(encryptorService);
    }

    private JPanel panel;
    private JButton browseInputButton;
    private JTextField inputFileField;
    private JButton browseOutputButton;
    private JTextField outputFileField;

    private JPasswordField passwordField;

    private JButton encryptButton;
    private JButton decryptButton;

    private JLabel inputFileLabel;
    private JLabel passwordLabel;
    private JLabel outputFileLabel;
}
