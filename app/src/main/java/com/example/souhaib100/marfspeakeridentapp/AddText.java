package com.example.souhaib100.marfspeakeridentapp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AddText {

    private File yourFile; //add the name of your file in the brackets
    private int numLines; //this will store the number of lines in the file
    private String[] lines; //the lines of text that make up the file will be stored here
    private int line_num;
    String text;

    public AddText(String file_name, int numLines, int line_num, String text) {
        this.yourFile = new File(file_name);
        this.numLines = numLines;
        this.line_num = line_num;
        this.text = text;
        lines = new String[this.numLines];//here we set the size of the array to be the same as the number of lines in the file
        for (int count = 0; count < this.numLines; count++) {
            lines[count] = readLine(count, yourFile);//here we set each string in the array to be each new line of the file
        }
        doSomethingToStrings();
    }


    private String readLine(int lineNumber, File aFile) {
        String lineText = "";
        try {

            BufferedReader input = new BufferedReader(new FileReader(aFile));
            try {
                for (int count = 0; count < lineNumber; count++) {
                    input.readLine();  //ReadLine returns the contents of the next line, and returns null at the end of the file.
                }
                lineText = input.readLine();
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return lineText;
    }

    private void doSomethingToStrings() {
        try {
            if (!lines[line_num].endsWith("_"))
                lines[line_num] = lines[line_num].concat("|" + text); //this joins the two strings for training + 1
            else
                lines[line_num] = lines[line_num].concat("," + text); //this joins the two strings for first training

        } catch (ArrayIndexOutOfBoundsException ex) { // I have added a try{}catch{} block so that if there is not as many lines in the file as expected, the code will still continue.

        }
        try {
            writeFile(yourFile);
        } catch (FileNotFoundException ex) {
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void writeFile(File aFile) throws IOException {
        if (aFile == null) {
            throw new IllegalArgumentException("File should not be null.");
        }
        if (!aFile.exists()) {
            throw new FileNotFoundException("File does not exist: " + aFile);
        }
        if (!aFile.isFile()) {
            throw new IllegalArgumentException("Should not be a directory: " + aFile);
        }
        if (!aFile.canWrite()) {
            throw new IllegalArgumentException("File cannot be written: " + aFile);
        }

        BufferedWriter output = new BufferedWriter(new FileWriter(aFile));
        try {
            for (int count = 0; count < numLines; count++) {
                output.write(lines[count]);
                if (count != numLines - 1) {// This makes sure that an extra new line is not inserted at the end of the file
                    output.newLine();
                }
            }
        } finally {
            output.close();
        }
    }
}
