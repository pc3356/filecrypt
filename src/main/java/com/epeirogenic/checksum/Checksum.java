package com.epeirogenic.checksum;

import java.io.File;
import java.nio.file.Files;
import java.security.MessageDigest;
import lombok.extern.slf4j.Slf4j;

/**
 * @see "http://www.rgagnon.com/javadetails/java-0416.html"
 */
@Slf4j
public enum Checksum {

    MD5("MD5"),
    SHA1("SHA1"),
    SHA256("SHA-256");

    private final String algorithm;

    Checksum(String algorithm) {
        this.algorithm = algorithm;
    }

    private byte[] createChecksum(File file) throws Exception {
        var fis = Files.newInputStream(file.toPath());

        var buffer = new byte[1024];
        var complete = MessageDigest.getInstance(algorithm);
        int numRead;
        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);
        fis.close();
        return complete.digest();
    }

    public String generateFor(final File file) throws Exception {

        return getHex(createChecksum(file));
    }

//    public static void main(final String args[]) {
//
//        try {
//            for(String filename : args) {
//                System.out.println(filename + " : " + Checksum.MD5.generateFor(new File(filename)));
//            }
//        }
//        catch (Exception e) {
//            e.getStackTrace();
//        }
//    }

    private static final String HEXES = "0123456789ABCDEF";

    private String getHex(final byte[] checksumBytes) {
        if ( checksumBytes == null ) {
            return null;
        }
        var hex = new StringBuilder( 2 * checksumBytes.length );
        for ( final byte b : checksumBytes ) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4))
                    .append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

//    private String getHexString(final byte[] checksumBytes) {
//        var result = new StringBuilder();
//        for (var b : checksumBytes) {
//            result.append(Integer.toString( ( b & 0xff ) + 0x100, 16).substring( 1 ));
//        }
//        return result.toString();
//    }
}