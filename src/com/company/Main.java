package com.company;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static File inputFile = null;

    private static int numberOfLines = 0;
    private static final ArrayList<File> files = new ArrayList<>();

    private static boolean toFindTitle = false;
    private static boolean toFindTableOfContents = false;
    private static boolean toFindVersions = false;
    private static boolean toFindRevisions = false;
    private static boolean toFindBibliography = false;
    private static boolean toFindOther = false;

    private static final JSONObject jsonObject = new JSONObject();
    private static String titleStringToJson = "";
    private static JSONArray jsonTable = new JSONArray();
    private static final JSONObject jsonVersions = new JSONObject();
    private static final JSONObject jsonRevisions = new JSONObject();
    private static final JSONObject jsonBibliography = new JSONObject();
    private static final JSONObject jsonOther= new JSONObject();

    public static void main(String[] args) throws Exception { // Mikita
//        inputArgumentsExecutor(inputTest());
        inputArgumentsExecutor(args);
        exportToJSON();
    }

    private static String getSearchString(File inputFile, int startLine) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        String ls = System.getProperty("line.separator");
        int i = 0;
        while (i < startLine) {
            reader.readLine();
            i++;
        }
        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
            i++;
        }
        if (stringBuilder.length() > 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        reader.close();
        return stringBuilder.toString();
    }

    private static void findTitle() { //Marek
        Map<String, String> patternMap = new HashMap<>();
        patternMap.put("title1", "^[\\s\\S]*?(?=\\bSecurity Target Lite\\b)");
        patternMap.put("title2", "^[\\s\\S]*?(?=\\bfrom\\b)");
        ArrayList<String> matches = new ArrayList<>();
        patternMap.forEach((key, value) -> {
            Pattern pattern = Pattern.compile(value);
            String content = null;
            try {
                content = getSearchString(inputFile, 0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert content != null;
            Matcher matcher = pattern.matcher(content);
            while (matcher.find()) {
                if (!matches.contains(matcher.group())) {
                    matches.add(matcher.group());
                }
            }
        });

        if (!matches.isEmpty()) {
            String result = matches.get(0);
            String[] parts = result.split("\\n{3,}");
            result = parts[0];
            result = result.replaceAll("[ ]+", " ");
            result = result.replaceAll("[\\s|\\t\\r\\n]+", " ").trim();
            titleStringToJson = result;
        }
    }

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
            jsonArray.addAll(matches);
            if(!jsonArray.isEmpty()){
                jsonVersions.put(key, jsonArray);
            }
        });
    }

    private static void findBibliography() throws IOException { //Kunal
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
        Pattern pattern = Pattern.compile("[A-Z1-9]+.*\\s(Bibliography[^.]*)|(BIBLIOGRAPHY[^.]*)");
//        Pattern endPattern = Pattern.compile("^[\\s\\S]*?(?=\\n\\n)");
        Pattern bibliographyEntriesPattern = Pattern.compile("\\s+\\[[0-9]+\\].+(?!\\[[1-9]+\\])");
        int i = 1;

        do {
            for (; i < numberOfLines; ++i) {
                String currentLine = reader.readLine();
                Matcher matcher = pattern.matcher(currentLine);
                if (matcher.find()) {
                    break;
                }
            }

            int maxContentsLines = 500;
            if (i + maxContentsLines > numberOfLines) {
                maxContentsLines = numberOfLines - i - 1;
            }
            if (i + maxContentsLines <  numberOfLines) {
                String searchString = getSearchString(inputFile, i);
                searchString = searchString.replaceAll("\\n{2,}", "--- new lines ---");
                searchString = searchString.replace("\n", " ");
                searchString = searchString.replaceAll(" +", " ");
                Matcher matcher = bibliographyEntriesPattern.matcher(searchString);
                if (matcher.find()) {
                    ArrayList<String> entries = parseBibliography(matcher.group());
                    for (int index = 0; index < entries.size()-1; index++) {
                        String tag = entries.get(index);
                        String value = entries.get(++index);
                        tag = tag.replace("--- new lines ---", "");
                        value = value.replace("--- new lines ---", "");
                        jsonBibliography.put(tag, value);
                    }
                    break;
                }

            }
        } while (i < numberOfLines);

    }

    private static ArrayList<String> parseBibliography(String content) {
        ArrayList<String> result = new ArrayList<>();
        String[] entries;
        entries = content.split("(?=\\[[0-9a-zA-Z]+\\])");
        for (String line : entries) {
            String[] parts = line.split("(?<=\\])");
            Collections.addAll(result, parts);
        }
        if (result.size() > 0 && result.get(0).equals(" ")) {
            result.remove(0);
        }
        if (result.size() > 0) {
            String lastLine = result.get(result.size()-1);
            result.remove(result.size()-1);
            String[] parts = lastLine.split("(--- new lines ---)");
            result.add(parts[0]);
        }
        return result;
    }

    private static void findTableOfContents() throws Exception { // Marek
        //TODO
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile)));
        Pattern pattern = Pattern.compile("(Contents)|(CONTENTS)|(INDEX)");
        Pattern pattern2 = Pattern.compile("^[\\s\\S]*?(?=\\n\\n)");
        int i = 1;
        for (; i < numberOfLines + 1; ++i) {
            String currentLine = reader.readLine();
            Matcher matcher = pattern.matcher(currentLine);
            if (matcher.find()) {
                break;
            }
        }

        int maxContentsLines = 500;
        if (i + maxContentsLines > numberOfLines) {
            maxContentsLines = numberOfLines - i;
        }
        if (i + maxContentsLines <  numberOfLines) {
            String searchString = getSearchString(inputFile, i);
            Matcher matcher = pattern2.matcher(searchString);
            while (matcher.find()) {
                sanitizeTOC(matcher.group());
                break;
            }
        }
    }

    private static void sanitizeTOC(String result) {
        result = result.replaceAll("[.]{2,}", "@"); //replace 2+ dots with a single space
        result = result.replaceAll("[ ]+", " ").trim(); //replace multiple spaces by one space
        result = result.replaceAll("\\n\\s", "\n");
        result = result.replaceAll("[\\s|\\t\\r\\n]+", " ").trim();
        String[]lines = result.split("\\n");
        StringBuilder finalEntry = new StringBuilder("[");
        for (String line : lines) {
            String[] pageNumberSplit = line.split("@");
            String[] idSplit = pageNumberSplit[0].split(" ");
            String id = idSplit[0];
            StringBuilder name = new StringBuilder();
            for (int i = 1; i < idSplit.length; i++) {
                name.append(idSplit[i]);
            }
            try {
                int pageNumber = Integer.parseInt(pageNumberSplit[pageNumberSplit.length - 1]);
                String entry = "[\"" + id + "\", \"" + name + "\", " + pageNumber + "],";
                finalEntry.append(entry);
            } catch (NumberFormatException e) {
                System.err.println("Error: Table of content, number format exception.");
            }
        }

        finalEntry = new StringBuilder(finalEntry.substring(0, finalEntry.length() - 1));
        if (finalEntry.length() == 0) {
            finalEntry.append("[");
        }
        finalEntry.append("]");

        jsonTable = (JSONArray) JSONValue.parse(finalEntry.toString());
    }

    private static void findRevisions() { // ?
    }

    private static void findOther() { // Mikita
        Map<String, String> patternMap = new HashMap<>();
        patternMap.put("certid", "NSCIB[\\w-]{3,}");

        for (Map.Entry<String, String> entry : patternMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
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
            jsonArray.addAll(matches);
            if (!jsonArray.isEmpty()) {
                jsonOther.put(key, jsonArray);
            }
        }
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
            jsonObject.put("title", titleStringToJson);
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
        else {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String jsonOutput = gson.toJson(jsonObject);
            file.write(jsonOutput);
            System.out.println(jsonFileName + " was successfully created in root directory.");
        }

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
            exportToJSON();
        }
    }

    private static void inputArgumentsInspector(String[] args) { //Mikita
        boolean correctArguments = true;
        String correctInputExample = "Example: text.txt --Title --Table_of_contents file.txt [...] (or \"exit\").";
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
            if (string.matches("(?i)--All")) {
                toFindTitle = true;
                toFindTableOfContents = true;
                toFindRevisions = true;
                toFindBibliography = true;
                toFindVersions = true;
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

    private static String[] inputTest(){
        return new String[]{
                "./InputFiles/[ST-Lite-EAC]_(v1.1)_2018_2000036361_-_Security_Target_Lite_IDeal_Pass_v2.3-n_(SAC_EAC_Polymorphic).txt",
                "./InputFiles/[ST-Mercury]_Security_Target_Mercury_v3.5.txt",
                "./InputFiles/0782V5b_pdf.txt",
                "./InputFiles/0879V4a_pdf.txt",
                "./InputFiles/0939V3b_pdf.txt",
                "./InputFiles/0945V3a_pdf.txt",
                "./InputFiles/0961V3a_pdf.txt",
                "./InputFiles/0977V2a_pdf.txt",
                "./InputFiles/0977V2b_pdf.txt",
                "./InputFiles/1019V2b_pdf.txt",
                "./InputFiles/1022a_pdf.txt",
                "./InputFiles/1022b_pdf.txt",
                "./InputFiles/1025V3a_pdf.txt",
                "./InputFiles/1040b_pdf.txt",
                "./InputFiles/1051a_pdf.txt",
                "./InputFiles/1059b_pdf.txt",
                "./InputFiles/1072a_pdf.txt",
                "./InputFiles/1086b_pdf.txt",
                "./InputFiles/1098a_pdf.txt",
                "./InputFiles/1102a_pdf.txt",
                "./InputFiles/1105b_pdf.txt",
                "./InputFiles/1107a_pdf.txt",
                "./InputFiles/1107b_pdf.txt",
                "./InputFiles/1110V2a_pdf.txt",
                "./InputFiles/1110V3b_pdf.txt",
                "./InputFiles/1126a_pdf.txt",
                "./InputFiles/1126b_pdf.txt",
                "./InputFiles/anssi-cible-cc-2020_72en.txt",
                "./InputFiles/Certification_Report_NSCIB-CC-67206-CR4.1.txt",
                "./InputFiles/Certification_Report_NSCIB-CC-200689-CR.txt",
                "./InputFiles/Certification_Report_NSCIB-CC-200736-CR.txt",
                "./InputFiles/certification-report-nscib-cc-180212-cr2.txt",
                "./InputFiles/NSCIB-CC_0075541-ST.txt",
                "./InputFiles/NSCIB-CC-0011955-CR.txt",
                "./InputFiles/NSCIB-CC-0023577-CR2-1.0.txt",
                "./InputFiles/NSCIB-CC-0075446-CRv2.txt",
                "./InputFiles/NSCIB-CC-0095534-CR.txt",
                "./InputFiles/NSCIB-CC-0095534-STLite.txt",
                "./InputFiles/NSCIB-CC-0112113-CR.txt",
                "./InputFiles/NSCIB-CC-0145426-ST_rev_C-final.txt",
                "./InputFiles/NSCIB-CC-0145427-CR.txt",
                "./InputFiles/NSCIB-CC-156530_3-STv3.2.txt",
                "./InputFiles/NSCIB-CC-156530-CR3.txt",
                "./InputFiles/NSCIB-CC-217812-CR2.txt",
                "./InputFiles/nscib-cc-0229285-creac.txt",
                "./InputFiles/nscib-cc-0229285eac-stv1.2.txt",
                "./InputFiles/nscib-cc-0229286sscdkeygen-stv1.2.txt",
                "./InputFiles/NSCIB-CC-0229287-CR_(SSCD-IMP)_NO_eIDAS.txt",
                "./InputFiles/NSCIB-CC-0229287(SSCDkeyImp)-STv1.2.txt",
                "./InputFiles/example1.txt",
                "./InputFiles/example2.txt",
                "./InputFiles/Mikita1.txt",
                "./InputFiles/Mikita2.txt",
                "./InputFiles/SecurityTargetLite_MF2DL_MF2ID_NTAG42x(Tf)_v1.0.txt",
                "--all"
        };
    }
}
