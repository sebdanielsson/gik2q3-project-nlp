package com.sebdanielsson;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.FSDataOutputStream;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.ling.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.net.URI;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        // Check if the user provided the input and output directory paths
        if (args.length < 2) {
            System.out.println("Usage: java -jar <jar-file> <input-directory> <output-directory>");
            return;
        }

        String inputDirectoryPath = args[0];
        String outputDirectoryPath = args[1];
        System.out.println("Input Path: " + args[0]);
        System.out.println("Output Path: " + args[1]);

        // Set up the Hadoop configuration and file system object
        Configuration conf = new Configuration();
        FileSystem fs = null;
        try {
            fs = FileSystem.get(URI.create(inputDirectoryPath), conf);
            RemoteIterator<LocatedFileStatus> fileStatusListIterator = fs.listFiles(new Path(inputDirectoryPath),
                    false);
            while (fileStatusListIterator.hasNext()) {
                LocatedFileStatus fileStatus = fileStatusListIterator.next();
                // Check if file is a .txt file
                if (fileStatus.getPath().getName().endsWith(".txt")) {
                    BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(fileStatus.getPath())));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line).append(" ");
                    }
                    br.close();

                    // Process the text content and write to output file
                    String textContent = sb.toString();
                    String outputFileName = fileStatus.getPath().getName().replace(".txt", "_processed.txt");
                    Path outputPath = new Path(outputDirectoryPath + "/" + outputFileName);
                    processTextAndWriteToFile(textContent, fs, outputPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fs != null) {
                try {
                    fs.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void processTextAndWriteToFile(String text, FileSystem fs, Path outputPath) {
        // Set up pipeline properties
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma");

        // Initialize the pipeline with the properties
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Create a document object
        CoreDocument document = new CoreDocument(text);

        // Annotate the document
        pipeline.annotate(document);

        // StringBuilder to collect the processed text
        StringBuilder processedText = new StringBuilder();

        // Iterate over the sentences in the document
        for (CoreSentence sentence : document.sentences()) {
            // Iterate over the tokens in the sentence
            for (CoreLabel token : sentence.tokens()) {
                String lemma = token.lemma(); // Lemmatized word
                // Only append words that are not punctuation (you can enhance this check as
                // needed)
                if (!lemma.matches("\\p{Punct}")) {
                    processedText.append(lemma).append(" ");
                }
            }
        }

        // Trim the string to remove the last space and convert to lowercase
        String finalText = processedText.toString().trim().toLowerCase();

        // Write the processed text to the output file
        try (FSDataOutputStream outputStream = fs.create(outputPath);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            bw.write(finalText);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
