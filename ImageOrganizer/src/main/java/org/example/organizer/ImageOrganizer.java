package org.example.organizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ImageOrganizer {
  
  public static String srcFolderPath;
  
  public static void main(String[] args) {
    if (args[0] == null) {
      System.out.println("Source folder path is required.");
      return;
    }

    srcFolderPath = args[0];
    
    try {
      work(srcFolderPath);
    } catch (Exception ex) {
      System.err.println(ex.getMessage());
    }
  }
  
  public static void work(String srcFolderPath) throws IOException {
    if (srcFolderPath == null) {
      throw new IOException("Source folder path is required.");
    }
    
    File folder = new File(srcFolderPath);
    if (!folder.isDirectory()) {
      throw new IOException("Given path is not a folder.");
    }
    
    File[] files = folder.listFiles();
    Map<String, HashSet<String>> imageFiles = organizeImages(files);
    copyImagesToBaseFolders(imageFiles);
  }
  
  public static String getBaseName(String fileName) {
    int index = fileName.lastIndexOf('.');
    if (index == -1) {
      // File with no extension.
      return fileName;
    }
    
    String nameWithoutExtension = fileName.substring(0, index);
    String baseName = nameWithoutExtension.replaceAll("\\s*\\(.*\\)$", "");
    return baseName;
  }
  
  public static Map<String, HashSet<String>> organizeImages(File[] files) throws IOException {
    if (files == null || files.length == 0) {
      throw new IOException("The given path doesn't exist or is empty.");
    }
    
    Map<String, HashSet<String>> imagesMap = new HashMap<>();
    for (File file : files) {
      if (!file.isFile()) {
        continue;
      }
      
      String fileName = file.getName();
      String baseName = getBaseName(fileName);
      
      if (baseName == null) {
        continue;
      }
      
      if (imagesMap.containsKey(baseName)) {
        // If the baseName key exist, put the fileName in the HashSet.
        imagesMap.get(baseName).add(fileName);
      } else {
        // If the baseName key doesn't exist, create a new HashSet, add the image in it and then put the key and HashSet into the Map.
        HashSet<String> baseNameSet = new HashSet<>();
        baseNameSet.add(fileName);
        imagesMap.put(baseName, baseNameSet);
      }
    }
    
    return imagesMap;
  }
  
  public static void copyImagesToBaseFolders(Map<String, HashSet<String>> imagesMap) {
    if (imagesMap == null) {
      throw new IllegalArgumentException("Images HashMap is null.");
    }
    
    for (String key : imagesMap.keySet()) {
      Set<String> images = imagesMap.get(key);
      
      File destFolder = new File(srcFolderPath, key);
      try {
        Files.createDirectory(destFolder.toPath());
      } catch (IOException e) {
        System.err.println(e);
      }
      
      for (String imageName: images) {
        File image = new File(srcFolderPath, imageName);
        Path destinationPath = Path.of(destFolder.getPath(), imageName);
        
        try {
          Files.copy(image.toPath(), destinationPath);
        } catch (IOException e) {
          System.err.println("An error has occurred with copying image '" + imageName + "'. \nCause: " + e.getMessage());
        }
      }
    }
  }
}