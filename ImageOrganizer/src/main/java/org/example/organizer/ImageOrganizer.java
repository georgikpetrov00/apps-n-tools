package org.example.organizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ImageOrganizer {
  
  public static String srcFolderPath;
  public static boolean doTrace = false;
  public static int numberOfImagesLoaded = 0;
  public static int numberOfAlreadyExistingImages = 0;
  
  public static void main(String[] args) {
    System.out.println("==========Begin==========");
    
    if (args == null || args.length == 0) {
      System.out.println("=====How to use=====");
      System.out.println("arg 1*           - path to the images folder");
      System.out.println("arg 2 (optional) - use 'trace-on' for tracing");
      return;
    }
    
    if (args[0] == null) {
      System.out.println("Source folder path is required.");
      return;
    }

    srcFolderPath = args[0];
    
    if (args.length > 1 && args[1].equals("trace-on")) {
      doTrace = true;
    }
    
    try {
      work(srcFolderPath);
    } catch (Exception ex) {
      System.err.println(ex.getMessage());
    }
    
    printResults();
    
    System.out.println("==========End==========");
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
      
      numberOfImagesLoaded += 1;
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
        if (Files.exists(destFolder.toPath())) {
          if (doTrace) {
            System.out.println("Folder '" + key + "' already exist.");
          }
        } else {
          Files.createDirectory(destFolder.toPath());
          if (doTrace) {
            System.out.println("Folder '" + key + "' created.");
          }
        }
      } catch (IOException e) {
        System.err.println(e);
      }
      
      for (String imageName: images) {
        if (doTrace) {
          System.out.println("Preparing to copy image: '" + imageName + "'.");
        }
        File image = new File(srcFolderPath, imageName);
        
        if (doTrace) {
          System.out.println("Image will be copied to: '" + destFolder.getPath());
        }
        Path destinationPath = Path.of(destFolder.getPath(), imageName);
        
        try {
          Files.copy(image.toPath(), destinationPath);
          if (doTrace) {
            System.out.println("Image '" + imageName + "' successfully copied to '" + destinationPath + "'." );
          }
        } catch (FileAlreadyExistsException faee) {
          if (doTrace) {
            System.out.println("Image already exist: '" + imageName + "'.");
          }
          numberOfAlreadyExistingImages += 1;
        } catch (IOException e) {
          System.err.println("An error has occurred with copying image '" + imageName + "'.");
          System.err.println("Reason: " + e);
        }
        
        if (doTrace) {
          System.out.println(System.lineSeparator());
        }
      }
    }
  }
  
  public static void printResults() {
    System.out.println(System.lineSeparator());
    System.out.println("Results:");
    System.out.println("Total number of loaded images: " + numberOfImagesLoaded);
    System.out.println("Number of new images in folders: " + (numberOfImagesLoaded - numberOfAlreadyExistingImages));
    System.out.println("Number of already existing images in folders: " + numberOfAlreadyExistingImages);
    System.out.println(System.lineSeparator());
  }
}