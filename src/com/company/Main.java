package com.company;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Main {
    static File inputFile = null;

    public static void main(String[] args) {
//      Expected input:
//      path title versions other
//      path all
        try{
            System.out.println("Path is:" + args[0]);
            importTxtFile(args[0]) ;

        } catch (Exception e) {
            System.out.println("Input error");
            e.printStackTrace();
        }

        jsonInitialization();

        if(args[1].contains("version")){ //.matches detects separate word
            System.out.println("version detected");
            findVersions();
        }

        exportToJSON();
    }

    private static void jsonInitialization(){ } //Marek ...

    private static void findTitle(){ } //Marek

    private static void findVersions(){

    } //Mikita

    private static void findBibliography(){ } //Kunal

    private static void findTableOfContents(){ }

    private static void findRevisions(){ }

    private static void findOther(){ }

    private static void exportToJSON(){ }

    private static void importTxtFile(String path){
        try {
            inputFile = new File(path);

            Scanner myReader = new Scanner(inputFile); //file output for example
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                System.out.println(data);
            }
            myReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found");
            e.printStackTrace();
        }
    }
}