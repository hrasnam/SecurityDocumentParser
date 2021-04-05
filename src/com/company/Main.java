package com.company;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//      Expected input:
//      path title versions other
//      path all

public class Main {
    static File inputFile = null;
    static int numberOfLines = 0;

    public static void main(String[] args) throws Exception {
        importPdfFile(args[0]);
        jsonInitialization();
        numberOfLines = countLines(inputFile);

        if (args[1].contains("version")) { //.matches detects separate word
            System.out.println("version detected");
            findVersions();
        }

        if (args[1].contains("table")) { //.matches detects separate word
            System.out.println("table detected");
            findTableOfContents();
        }

        if (args[1].contains("all")) { //.matches detects separate word
            System.out.println("all detected");
            findVersions();
            findTableOfContents();
            findTitle();
            findBibliography();
            findOther();
            findRevisions();
        }

        exportToJSON();
    }

    private static void jsonInitialization() {
    } //Marek ...

    private static void findTitle() {
    } //Marek

    private static void findVersions(){ // Mikita
        Map<String, String> patternMap = new HashMap<>();
        patternMap.put("eal", "EAL+\\s?+[0-9]+\\+?");
        patternMap.put("global_platform", "GlobalPlatform\\s?\\d\\.\\d\\.\\d");
        patternMap.put("java_card", "Java\\s?Card\\s?(Platform)?\\s?v?\\d\\.?\\d?\\.?\\.?\\d?");
        patternMap.put("sha", "(SHA|sha)-\\d++");
        patternMap.put("rsa", "(RSA|rsa)[\\s|-]\\d\\d\\d\\d?");
        patternMap.put("ecc", "ECC[\\s|-]\\d\\d\\d");
        patternMap.put("des", "[0-9]-?DES");

        patternMap.forEach((key, value) -> {
            ArrayList<String> matches = new ArrayList<>();
            Pattern pattern = Pattern.compile(value);
            System.out.println(key + ":");
            Iterator<String> lineIterator = null;
            try {
                lineIterator = Files.lines(Paths.get(inputFile.getPath())).iterator();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (true) {
                assert lineIterator != null;
                if (!lineIterator.hasNext()) break;
                String line = lineIterator.next();
                Matcher matcher = pattern.matcher(line);
                while(matcher.find()){
                    if(!matches.contains(matcher.group())){
                        matches.add(matcher.group());
                    }
                }
            }
            matches.forEach(System.out::println);
        });
    }

    private static void findBibliography() {			//Kunal
int startingLine=0, endingLine=0, temp=0;
	BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
        for(int i = 1; i < numberOfLines+1; ++i){
            String currentLine = reader.readLine();
		Pattern pattern = Pattern.compile("(Bibliography)|(BIBLIOGRAPHY)|(INDEX)");
            Matcher matcher = pattern.matcher(currentLine);
            
		while(matcher.matches()){
                startingLine=i;
		break;
            }
        }
	BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
	String currentLineN ="";
	for(int i = 1; i < numberOfLines+1; ++i){
            currentLineN = reader2.readLine();
		Pattern pattern = Pattern.compile("(Bibliography)|(BIBLIOGRAPHY)|(INDEX)");
            Matcher matcher = pattern.matcher(currentLineN);
            
		while(matcher.find()){
		temp=i;
		currentLineN=reader2.readLine();
		break;
            }
        }
	String[] nextHeading=currentLineN.split(" ");
	String nextFindHeading=nextHeading[0];

	BufferedReader reader3 = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
	for(int i = 1; i < numberOfLines+1; ++i){
            String currentLine = reader3.readLine();
		Pattern pattern = Pattern.compile(nextFindHeading);
            Matcher matcher = pattern.matcher(currentLine);
            
		while(matcher.matches() && temp!=i){
                endingLine=i;
		break;
            }
        }
BufferedReader reader4 = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
		for(int i = 1; i < numberOfLines+1; ++i){
            String currentLine = reader4.readLine();
            while(i>=startingLine && i<endingLine){
                
                System.out.println(currentLine);
            }
        }

    }

    private static void findTableOfContents() throws Exception { // Mikita
        //TODO
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
        for(int i = 1; i < numberOfLines+1; ++i){
            String currentLine = reader.readLine();
            Pattern pattern = Pattern.compile("(Contents)|(CONTENTS)|(INDEX)");
            Matcher matcher = pattern.matcher(currentLine);
            while(matcher.find()){
                System.out.println("Line number: " + i);
                System.out.println(currentLine);
            }
        }

    }

    private static void findRevisions() {
    }

    private static void findOther() {
    }

    private static void exportToJSON() {
    }

    private static void importPdfFile(String path) throws IOException { //Mikita
        inputFile = new File(path);
        PDDocument doc = PDDocument.load(inputFile);
        PDFTextStripper stripper = new PDFTextStripper();

        File convertedToTxtFile = new File("./temp.txt");
        FileWriter writer = new FileWriter(convertedToTxtFile);
        writer.write(stripper.getText(doc));
        writer.flush();

        inputFile = convertedToTxtFile;
    }

    public static int countLines(File file) throws IOException { //Mikita
        try (InputStream is = new BufferedInputStream(new FileInputStream(file))) {
            byte[] c = new byte[1024];
            int readChars = is.read(c);
            if (readChars == -1)
                return 0;
            int count = 0;
            while (readChars == 1024) {
                for (int i = 0; i < 1024; )
                    if (c[i++] == '\n')
                        ++count;
                readChars = is.read(c);
            }
            while (readChars != -1) {
                for (int i = 0; i < readChars; ++i)
                    if (c[i] == '\n')
                        ++count;
                readChars = is.read(c);
            }
            return count == 0 ? 1 : count;
        }
    }
}