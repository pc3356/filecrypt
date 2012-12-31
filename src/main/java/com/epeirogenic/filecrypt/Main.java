package com.epeirogenic.filecrypt;

import org.apache.commons.io.FileUtils;

import java.io.File;

public class Main {

	public static void main(String[] args) throws Exception {


		File file = new File(args[0]);
		byte[] bytes = FileUtils.readFileToByteArray(file);




	}
}
