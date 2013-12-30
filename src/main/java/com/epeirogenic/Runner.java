package com.epeirogenic;

import com.epeirogenic.filecrypt.FileCryptService;

import java.io.File;

public class Runner {

    public static void main(String[] args) throws Exception {

        if(args.length < 3) {
            System.err.println("Usage: cmd <operation - 'en' or 'de'> <source file or directory> <password> [<output directory, default .>]");
            return;
        }

        File input = new File(args[1]);
        File output = (args.length == 3 ? input : new File(args[3]));

        FileCryptService service = new FileCryptService();
        if("en".equalsIgnoreCase(args[0])) {
            System.out.println("Encrypting " + input.getCanonicalPath() + " to " + output.getCanonicalPath());
            service.encrypt(args[2], input, output);
        } else if("de".equalsIgnoreCase(args[0])) {
            System.out.println("Decrypting " + input.getCanonicalPath() + " to " + output.getCanonicalPath());
            service.decrypt(args[2], input, output);
        }
        System.out.println("Done");
    }
}
