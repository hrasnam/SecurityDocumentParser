package com.company;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    static File inputFile = null;

    static int numberOfLines = 0;
    static ArrayList<File> files = new ArrayList<>();

    private static boolean toFindTitle = false;
    private static boolean toFindTableOfContents = false;
    private static boolean toFindVersions = false;
    private static boolean toFindRevisions = false;
    private static boolean toFindBibliography = false;
    private static boolean toFindOther = false;

    private static final JSONObject jsonObject = new JSONObject();
    private static String titleStringToJson = "";
    private static final JSONObject jsonTable = new JSONObject();
    private static final JSONObject jsonVersions = new JSONObject();
    private static final JSONObject jsonRevisions = new JSONObject();
    private static final JSONObject jsonBibliography = new JSONObject();
    private static final JSONObject jsonOther= new JSONObject();

    public static void main(String[] args) throws Exception { // Mikita
        inputArgumentsExecutor(args);

        exportToJSON();
    }

    private static String getTitleSearchString(File inputFile, int numberOfLines) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        StringBuilder stringBuilder = new StringBuilder();
        String line = null;
        String ls = System.getProperty("line.separator");
        int i = 0;
        while ((line = reader.readLine()) != null && i < numberOfLines) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
            i++;
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        reader.close();
        return stringBuilder.toString();
    }

    private static void findTitle() { //Marek
        Map<String, String> patternMap = new HashMap<>();
        patternMap.put("title1", "^[\\s\\S]*?(?=\\bSecurity Target Lite\\b)");
        patternMap.put("title2", "^[\\s\\S]*?(?=\\bfrom\\b)");
        //add more cases
        int titleLinesLimiter = 40; //max first 40 lines
        ArrayList<String> matches = new ArrayList<>();
        patternMap.forEach((key, value) -> {
            Pattern pattern = Pattern.compile(value);
            System.out.println(key + ":");
            String content = null;
            try {
                content = getTitleSearchString(inputFile, titleLinesLimiter);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Matcher matcher = pattern.matcher(content);
                while (matcher.find()) {
                    if (!matches.contains(matcher.group())) {
                        matches.add(matcher.group());
                    }
                }
                matches.forEach(System.out::println);
            });

        if (!matches.isEmpty()) {
            titleStringToJson = matches.get(0);
        }
    }

    private static void findVersions() { // Mikita
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
                while (matcher.find()) {
                    if (!matches.contains(matcher.group())) {
                        matches.add(matcher.group());
                    }
                }
            }

            JSONArray jsonArray = new JSONArray();
            for(String string: matches){
                jsonArray.add(string);
            }
            if(!jsonArray.isEmpty()){
                jsonVersions.put(key, jsonArray);
            }
        });
    }

    private static void findBibliography() throws IOException { //Kunal
//        int startingLine = 0, endingLine = 0, temp = 0;
//        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
//        for (int i = 1; i < numberOfLines + 1; ++i) {
//            String currentLine = reader.readLine();
//            Pattern pattern = Pattern.compile("(Bibliography)|(BIBLIOGRAPHY)");
//            Matcher matcher = pattern.matcher(currentLine);
//
//            while (matcher.matches()) {
//                startingLine = i;
//                break;
//            }
//        }
//        BufferedReader reader2 = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
//        String currentLineN = "";
//        for (int i = 1; i < numberOfLines + 1; ++i) {
//            currentLineN = reader2.readLine();
//            Pattern pattern = Pattern.compile("(Bibliography)|(BIBLIOGRAPHY)|(INDEX)");
//            //TODO ERROR
//            Matcher matcher = pattern.matcher(currentLineN);
//            while (matcher.find()) {
//                temp = i;
//                currentLineN = reader2.readLine();
//                break;
//            }
//        }
//        String[] nextHeading = currentLineN.split(" ");
//        String nextFindHeading = nextHeading[0];
//
//        BufferedReader reader3 = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
//        for (int i = 1; i < numberOfLines + 1; ++i) {
//            String currentLine = reader3.readLine();
//            Pattern pattern = Pattern.compile(nextFindHeading);
//            Matcher matcher = pattern.matcher(currentLine);
//
//            while (matcher.matches() && temp != i) {
//                endingLine = i;
//                break;
//            }
//        }
//
//        BufferedReader reader4 = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
//        for (int i = 1; i < numberOfLines + 1; ++i) {
//            String currentLine = reader4.readLine();
//            while (i >= startingLine && i < endingLine) {
//                System.out.println(currentLine);
//            }
//        }

    }

    private static void findTableOfContents() throws Exception { // Mikita
        //TODO
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
        for (int i = 1; i < numberOfLines + 1; ++i) {
            String currentLine = reader.readLine();
            Pattern pattern = Pattern.compile("(Contents)|(CONTENTS)|(INDEX)");
            Matcher matcher = pattern.matcher(currentLine);
            while (matcher.find()) {
                System.out.println("Line number: " + i);
                System.out.println(currentLine);
            }
        }

    }

    private static void findRevisions() { // ?
    }

    private static void findOther() { // ?
    }

    private static void exportToJSON() throws IOException { // Mikita
        Pattern pattern = Pattern.compile("[ \\w-]+\\.");
        Matcher matcher = pattern.matcher(inputFile.getName());
        String jsonFileName;
        if(matcher.find()){
           jsonFileName = matcher.group() + "json";
        } else {
            jsonFileName = "output.json";
        }
        FileWriter file = new FileWriter(jsonFileName);

        if (toFindTitle && !(titleStringToJson == null || titleStringToJson.isEmpty() ))
            jsonObject.put("title", titleStringToJson); //not fully correct
        if (toFindTableOfContents && !jsonTable.toJSONString().equals("{}"))
            jsonObject.put("table_of_contents", jsonTable);
        if (toFindVersions && !jsonVersions.toJSONString().equals("{}"))
            jsonObject.put("versions", jsonVersions);
        if (toFindRevisions && !jsonRevisions.toJSONString().equals("{}"))
            jsonObject.put("revisions", jsonRevisions);
        if (toFindBibliography && !jsonBibliography.toJSONString().equals("{}"))
            jsonObject.put("bibliography", jsonBibliography);
        if (toFindOther && !jsonOther.toJSONString().equals("{}"))
            jsonObject.put("other", jsonOther);

        if (jsonObject.toJSONString().equals("{}"))
            System.err.println("Nothing to write to JSON, empty file saved");
        else
            file.write(jsonObject.toJSONString());

        file.close();
    }

    private static void inputArgumentsExecutor(String[] args) throws Exception { // Mikita
        inputArgumentsInspector(args);
        for (File file : files) {
            inputFile = file;
            numberOfLines = countLines(inputFile);

            if (toFindTitle)
                findTitle();
            if (toFindTableOfContents)
                findTableOfContents();
            if (toFindVersions)
                findVersions();
            if (toFindRevisions)
                findRevisions();
            if (toFindBibliography)
                findBibliography();
            if (toFindOther)
                findOther();
        }
    }

    private static void inputArgumentsInspector(String[] args) { //Mikita
        boolean correctArguments = true;
        String correctInputExample = "Example: text.txt --title --Table_of_contents file.txt [...] (or \"exit\").";
        while (true) {
            if (!correctArguments) {
                args = inputArgumentsReader();
            }
            if (args == null) {
                System.err.println("Input is empty, please, write your arguments again.");
                System.err.println(correctInputExample);
                correctArguments = false;
                continue;
            }
            if (!detectSubparts(args)) {
                System.err.println("Input finder didn't detect any subparts, please, write your arguments again.");
                System.err.println(correctInputExample);
                correctArguments = false;
                continue;
            }
            if (!detectFilePath(args)) {
                System.err.println("Input finder didn't detect any files, please, try again.");
                System.err.println(correctInputExample);
                correctArguments = false;
                continue;
            }
            break;
        }
    }

    private static String[] inputArgumentsReader() { //Mikita
        System.out.println("Write arguments again, please: ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String inputString = null;
        try {
            inputString = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert inputString != null;
        if(inputString.equals("exit"))
            System.exit(0);
        char[] array = inputString.toCharArray();
        ArrayList<String> list = new ArrayList<>();
        StringBuilder tempString = new StringBuilder();
        for (int i = 0; i < inputString.length(); i++) {
            if (array[i] == ' ' || i == inputString.length() - 1) {
                list.add(tempString.toString());
                tempString = new StringBuilder();
                continue;
            }
            tempString.append(array[i]);
        }
        String[] args = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            args[i] = list.get(i);
        }
        return args;
    }

    private static boolean detectFilePath(String[] args) { //Mikita
        boolean success = false;
        for (String string : args) {
            if (new File(string).isFile() &&  (string.charAt(0) != '-' && string.charAt(1) != '-')){
                files.add(new File(string));
                success = true;
            }
        }
        return success;
    }

    private static boolean detectSubparts(String[] args) { //Mikita
        boolean success = false;
        for (String string : args) {
            if (string.matches("(?i)--Title")) {
                toFindTitle = true;
                success = true;
            }
            if (string.matches("(?i)(--Table)(.of.contents)?")) {
                toFindTableOfContents = true;
                success = true;
            }
            if (string.matches("(?i)(--Versions)(.of.used.libraries)?")) {
                toFindVersions = true;
                success = true;
            }
            if (string.matches("(?i)--Revisions")) {
                toFindRevisions = true;
                success = true;
            }
            if (string.matches("(?i)--Bibliography")) {
                toFindBibliography = true;
                success = true;
            }
            if (string.matches("(?i)--Other")) {
                toFindOther = true;
                success = true;
            }
        }

        return success;
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