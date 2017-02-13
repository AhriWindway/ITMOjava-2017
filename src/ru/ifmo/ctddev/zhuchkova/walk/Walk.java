package ru.ifmo.ctddev.zhuchkova.walk;

/**
 * Created by Анастасия on 13.02.2017.
 */
import java.nio.file.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class Walk {
  public static void main(String[] args) {
    if (args == null) {
      System.err.println("Error: No input and output files given!");
      return;
    }
    
    if (args.length < 2) {
      System.err.println("Error: No input or output file given!");
      return;
    }
    
    try {
      Path[] files = {Paths.get(args[0]), Paths.get(args[1])};
      if (Files.notExists(files[0])) {
        System.err.println("Error: Input file doesn't exist!");
        return;
      }
      
      try {
        try (BufferedWriter out = Files.newBufferedWriter(files[1], StandardCharsets.UTF_8);
             BufferedReader in = Files.newBufferedReader(files[0], StandardCharsets.UTF_8)) {
          for(String line; (line = in.readLine()) != null; ) {
            File currentFile = new File(line); //
            out.write(String.format("%08x %s", fnv(currentFile), currentFile.getPath()));
            out.newLine();
          }
        }
      } catch (IOException e) {
        System.err.println("Error: " + e);
      }
    } catch (InvalidPathException e) {
      System.err.println("Error: Path string can't be converted to a Path!");
    }
  }

  private static int fnv(File file) {
    int hash = 0x811c9dc5, n;
    try {
      try (FileInputStream fileInputStream = new FileInputStream(file)) {
        byte b[] = new byte[2048];
        while ((n = fileInputStream.read(b)) >= 0) {
          for (int i = 0; i < n; i++) {
            hash = (hash * 0x01000193) ^ (b[i] & 0xff);
          }
        }
        return hash;
      }
    } catch (FileNotFoundException e) {
      System.err.println("Error: " + file.getPath() + " not found!");
      return 0;
    } catch (IOException e) {
      System.err.println("Error: " + e);
      return 0;
    }
  }
}