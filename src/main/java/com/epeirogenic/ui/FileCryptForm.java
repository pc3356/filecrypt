package com.epeirogenic.ui;

import com.epeirogenic.checksum.Checksum;
import com.epeirogenic.filecrypt.EncryptorService;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileFilter;
import java.util.List;

@Slf4j
public class FileCryptForm extends JDialog {

    private Worker worker;

    private File inputFile;

    private File outputFile;
    private final static String DEFAULT_EXTENSION = "fcf";

    private final EncryptorService encryptorService;

    public FileCryptForm() {
        this(new EncryptorService());
    }

    public FileCryptForm(final EncryptorService encryptorService) {

        this.encryptorService = encryptorService;
        createUIComponents();
    }

    private void createUIComponents() {

        log.info("Creating UI components");

        setContentPane(panel);
        setModal(true);

        if (browseInputButton != null) {
            browseInputButton.setEnabled(true);
            browseInputButton.addActionListener(
                    actionEvent -> chooseInputFile()
            );
        }

        if (browseOutputButton != null) {
            browseOutputButton.setEnabled(true);
            browseOutputButton.addActionListener(
                    actionEvent -> chooseOutputFile()
            );
        }

        if (encryptButton != null) {
            encryptButton.setEnabled(false);
            encryptButton.addActionListener(
                    actionEvent -> {
                        encrypt();
                        passwordField.setText("");
                    }
            );
        }

        if (decryptButton != null) {
            decryptButton.setEnabled(false);
            decryptButton.addActionListener(
                    actionEvent -> {
                        decrypt();
                        passwordField.setText("");
                    }
            );
        }

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                if (e != null) {
                    onCancel();
                }
            }
        });

        // call onCancel() on ESCAPE
        if (panel != null) {
            panel.registerKeyboardAction(e -> {
                if (e != null) {
                    onCancel();
                }
            }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        }

        worker = new Worker();
    }

    private void encrypt() {

        char[] password = prepareForOperation();
        if (inputFile.isDirectory()) {
            iterate(inputFile, password, false);
        } else {
            encryptFile(inputFile, password);
        }
        log.info("Encryption complete");
        statusField.setText("Encryption complete");
    }

    private void encryptFile(final File input, final char[] password) {
        performCryptOperation(input, password, false);
    }

    private void decrypt() {

        var password = prepareForOperation();
        if (inputFile.isDirectory()) {
            iterate(inputFile, password, true);
        } else {
            decryptFile(inputFile, password);
        }
        log.info("Decryption complete");
        statusField.setText("Decryption complete");
    }

    private void decryptFile(final File input, final char[] password) {
        performCryptOperation(input, password, true);
    }

    private void performCryptOperation(final File input, final char[] password, final boolean decrypt) {
        log.debug(password == null ? "PASSWORD IS NULL" : "PASSWORD OK");
        try {
            if (decrypt) {
                var output = createDecryptOutputFile(input);
                log.debug("Decrypting {}", input.getName());
                log.debug("To: {}", output.getName());
                statusField.setText(input.getName());
                encryptorService.decrypt(input, output, password);
                log.debug("Decrypted: {} to: {}", input.getAbsolutePath(), output.getAbsolutePath());
            } else {
                var output = createEncryptOutputFile(input);
                log.debug("Encrypting {}", input.getName());
                log.debug("To: {}", output.getName());
                statusField.setText(input.getName());
                encryptorService.encrypt(input, output, password);
                log.debug("Encrypted: {} to: {}", input.getAbsolutePath(), output.getAbsolutePath());
            }
        } catch (Exception e) {
            log.error("Error encrypting file {}", input.getName(), e);
        }
    }

    private File createEncryptOutputFile(File inputFile) {

        if (outputFile.isDirectory()) {
            return createChecksumFilename(inputFile);
        }
        return outputFile;
    }

    public File createDecryptOutputFile(File inputFile) {

        if (outputFile.isDirectory()) {
            return new File(outputFile, FilenameUtils.removeExtension(inputFile.getName()));
        } else {
            return outputFile;
        }
    }

    private File createChecksumFilename(File inputFile) {

        try {
            var checksum = Checksum.MD5;
            var checksumString = checksum.generateFor(inputFile);
            var extension = FilenameUtils.getExtension(inputFile.getName());

            log.debug("Checksum: {}", checksumString);
            log.debug("Extension: {}", extension);

            File path;
            if (outputFile.isDirectory()) {
                path = outputFile;
            } else {
                path = outputFile.getParentFile();
            }
            var fileName = checksumString + '.' + extension + '.' + DEFAULT_EXTENSION;
            log.debug("Filename: {}", fileName);

            var output = new File(path, fileName);

            log.debug("Output file (checksum name): {}", output.getAbsolutePath());

            return output;

        } catch (Exception e) {
            log.error("Unable to create output file", e);
            return null;
        }
    }

    private char[] prepareForOperation() {

        if (inputFile == null) {
            throw new IllegalArgumentException("No input file provided");
        }

        if (outputFile == null) {
            // set the output field + file to show input directory
            outputFile = (inputFile.isFile() ? inputFile.getParentFile() : inputFile);
            outputFileField.setText(outputFile.getAbsolutePath());
        }

        return passwordField.getPassword();
    }

    private void iterate(File input, char[] password, boolean decrypt) {

        if (input.isDirectory()) {
            final FileFilter fileFilter = FileFilterUtils.and(FileFileFilter.FILE, HiddenFileFilter.VISIBLE);
            var files = input.listFiles(fileFilter);

            assert files != null;
            for (var file : files) {
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
        if (inputFile != null) {
            inputFileField.setText(inputFile.getAbsolutePath());
            encryptButton.setEnabled(true);
            decryptButton.setEnabled(true);
        }
        log.info("Input: {}", inputFile);
        statusField.setText(inputFile.getName());
    }

    private void chooseOutputFile() {
        outputFile = chooseFile("Output", outputFile);
        if (outputFile != null) {
            outputFileField.setText(outputFile.getAbsolutePath());
        }
        statusField.setText("");
    }

    private File chooseFile(final String title, final File start) {
        log.debug("Open file dialog");

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Choose " + title + " file");

        fileChooser.setCurrentDirectory(determineStartDirectory(start));

        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setAcceptAllFileFilterUsed(false);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            log.debug("getCurrentDirectory(): {}", fileChooser.getCurrentDirectory());
            log.debug("getSelectedFile() : {}", fileChooser.getSelectedFile());

            return fileChooser.getSelectedFile();

        } else {
            log.debug("No file selection ");
            return null;
        }
    }

    private File determineStartDirectory(File file) {

        if (file == null) {
            return new File(System.getProperty("user.home", "/"));
        } else {
            if (file.isDirectory()) {
                return file;
            } else {
                return file.getParentFile();
            }
        }
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        panel = new JPanel();
        panel.setLayout(new GridLayoutManager(3, 3, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(4, 4, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        inputFileField = new JTextField();
        inputFileField.setEditable(false);
        panel1.add(inputFileField, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        passwordField = new JPasswordField();
        panel1.add(passwordField, new GridConstraints(2, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        inputFileLabel = new JLabel();
        inputFileLabel.setText("Input file");
        panel1.add(inputFileLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        passwordLabel = new JLabel();
        passwordLabel.setText("Password");
        panel1.add(passwordLabel, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        outputFileField = new JTextField();
        outputFileField.setEditable(false);
        panel1.add(outputFileField, new GridConstraints(1, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        outputFileLabel = new JLabel();
        outputFileLabel.setText("Output file");
        panel1.add(outputFileLabel, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        browseInputButton = new JButton();
        browseInputButton.setText("Browse");
        panel1.add(browseInputButton, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        browseOutputButton = new JButton();
        browseOutputButton.setText("Browse");
        panel1.add(browseOutputButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        encryptButton = new JButton();
        encryptButton.setText("Encrypt");
        panel1.add(encryptButton, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        decryptButton = new JButton();
        decryptButton.setText("Decrypt");
        panel1.add(decryptButton, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel3, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        final JPanel panel4 = new JPanel();
        panel4.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel4.setEnabled(true);
        panel.add(panel4, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        statusField = new JTextField();
        statusField.setBackground(new Color(-1118482));
        statusField.setDragEnabled(false);
        statusField.setEditable(false);
        panel4.add(statusField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        final JPanel panel5 = new JPanel();
        panel5.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        panel.add(panel5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return panel;
    }

    /* =============================================== */

    static class Worker extends SwingWorker<Void, String> {

        @Override
        protected Void doInBackground() throws Exception {
            log.info("doInBackground()");
            return null;
        }

        @Override
        protected void process(List<String> messages) {
            log.info("Process: {}", messages);
        }

        public void publish(String message) {
            super.publish(message);
        }

        @Override
        protected void done() {
            super.done();
        }

    }

    /* =============================================== */

//    public static void main(String[] args) {
//        var frame = new JFrame("FileCryptForm");
//        frame.setContentPane(new FileCryptForm().panel);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.setTitle("FileCrypt");
//        frame.setResizable(false);
//        frame.setLocation(new Point(500, 300));
//        frame.pack();
//        frame.setVisible(true);
//    }

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
    private JTextField statusField;
}
