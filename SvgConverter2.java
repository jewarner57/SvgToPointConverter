//Created by Jonathan Warner
import java.util.*; 
import java.nio.charset.StandardCharsets; 
import java.nio.file.*; 
import java.io.*; 
import java.awt.Color; 
import java.io.IOException; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.util.Enumeration; 
import java.util.HashMap; 
import java.util.TooManyListenersException;
import com.fazecast.jSerialComm.*;
import arduino.*;

public class SvgConverter2
{ 
  public static SerialPort getSerialPort() {
      if(SerialPort.getCommPorts().length < 1) {
          System.out.println("Please ensure a comm port is avaliable before running this program. \n");
      }
      return(SerialPort.getCommPorts()[0]);
  }
  
  public static List<String> readFileInList(String fileName) 
  { 
    List<String> lines = Collections.emptyList(); 
    try
    { 
      lines = 
       Files.readAllLines(Paths.get(fileName), StandardCharsets.UTF_8); 
    } 
  
    catch (IOException e) 
    { 
      e.printStackTrace(); 
    } 
    return lines; 
  }
  
  public static boolean isWordInLine(String word, String line) {
      if(line.length() >= word.length()) {
          for(int i = 0; i < line.length()-word.length()+1; i++) {
              if(line.substring(i, i+word.length()).equals(word)) {
                  //System.out.println(line.substring(i, i+word.length()) + " " + i);
                  return true;
              }
          }
      }
      return false;
  }
  
  public static int findPathEndingLine(List<String> lines, int startLine) {
     int endLine = startLine; 
     int currentLineNumber = startLine;
     boolean foundEnding = false;
     
     while(currentLineNumber < lines.size() && !foundEnding) {
         System.out.println(currentLineNumber + " " + lines.get(currentLineNumber));
         for(int i = 0; i < lines.get(currentLineNumber).length()-1; i++) {
             if(lines.get(currentLineNumber).substring(i, i+2).equals("/>")) {
                 System.out.println("found ending on this^ line.");
                 endLine = currentLineNumber;
                 foundEnding = true;
             }
         }
         currentLineNumber++;
     }
     
     System.out.println("Returned Endline as " + endLine +"\n");
     return endLine;
  }
  
  public static String extractPointDataFromPathStartAndPathEndPositions(List<String> lines, int startLine, int endLine) {
      String pointListString = "";
      String path = "";
      if(Math.abs(startLine - endLine) > 0) {
          path = combineLinesTogether(lines, startLine, endLine); 
      }
      else {
          path = lines.get(startLine);
      }
      
      System.out.println("Searching for points in: " + path);
      
      //find where points start
      int pointListStart = -1;
      int i = 0;
      while(pointListStart == -1) {
          if(path.substring(i, i+3).equals("d=\"")) {
              pointListStart = i+3;
              System.out.println("Found point list start to be: " + path.substring(i, i+3) + "->" + path.substring(i+3, i+4) + " \nFound substring position to be: " + pointListStart);
          }
          i++;
      }
      
      //find where points end
      int pointListEnd = -1;
      i = pointListStart;
      while(pointListEnd == -1) {
          if(path.substring(i, i+1).equals("\"")) {
              pointListEnd = i;
              System.out.println("Found point list end to be: " + path.substring(i, i+1) + " \nFound substring position to be: " + pointListEnd);
          }
          i++;
      }
      
      System.out.println("Found point list as: " + path.substring(pointListStart, pointListEnd));
      
      pointListString = path.substring(pointListStart, pointListEnd);
      
      return(pointListString);
  }    
  
  public static String combineLinesTogether(List<String> lines, int startLine, int endLine) {
      String path = "";
      
      for(int i = startLine; i <= endLine; i++) {
          path = path+lines.get(i);
      }
      System.out.println("\ncombined lines " + startLine + "-" + endLine + " into: " + path + "\n");
      return path;
  }
  
  public static List<String> extractPointsFromPointString(String pointString) {
      String points = pointString; 
      List<String> pointArray = new ArrayList<String>();
      
      int numberStartPosition = -1;
      int numberEndPosition = -1;
      
      for(int i = 0; i < pointString.length(); i++) {
          if(Character.isLetter(pointString.charAt(i))) {
              System.out.println("Character: " + pointString.substring(i, i+1) + " is a letter");
              pointArray.add(pointString.substring(i, i+1));
              System.out.println("Added character: " + pointString.substring(i, i+1));
              if(!(numberStartPosition == -1)) {
                  numberEndPosition = i;
                  pointArray.add(pointString.substring(numberStartPosition, numberEndPosition));
                  System.out.println("Added number:" + pointString.substring(numberStartPosition, numberEndPosition));
                  numberStartPosition = i+1;
              }
          }
          if(Character.isDigit(pointString.charAt(i)) || pointString.substring(i, i+1).equals("-") || pointString.substring(i, i+1).equals(".")) {
              System.out.println("Character: " + pointString.substring(i, i+1) + " is a digit");
              if(pointString.substring(i, i+1).equals("-")) {
                  if(numberStartPosition == -1) {
                      numberStartPosition = i;
                  }
                  else {
                      numberEndPosition = i;
                      pointArray.add(pointString.substring(numberStartPosition, numberEndPosition));
                      System.out.println("Added number:" + pointString.substring(numberStartPosition, numberEndPosition));
                      numberStartPosition = i;
                  }
              }
              if(numberStartPosition == -1) {
                  numberStartPosition = i;
              }
          }
          if(!(Character.isLetter(pointString.charAt(i)) || Character.isDigit(pointString.charAt(i))) && !(pointString.substring(i, i+1).equals("-") || pointString.substring(i, i+1).equals("."))) {
              System.out.println("This is not a letter or a digit: " + pointString.substring(i, i+1));
          
              if(numberStartPosition == -1) {
                      numberStartPosition = i+1;
                  }
              else {
                  numberEndPosition = i;
                  pointArray.add(pointString.substring(numberStartPosition, numberEndPosition));
                  System.out.println("Added number:" + pointString.substring(numberStartPosition, numberEndPosition));
                  numberStartPosition = i+1;
              }
          }
      }
      
      System.out.println("\n\n");
      for(int i = 0; i < pointArray.size(); i++) {
          System.out.print(pointArray.get(i) + ", " );
      }
      
      return pointArray;
  }
  
  public static void main(String args[]) {
      List l = readFileInList("./test.svg"); 
      Iterator<String> itr = l.iterator();
      List <String> finalPointList = Collections.emptyList();
      int currentLineNumber = 0;
      
      
      //search for beginning of a path object
      while (itr.hasNext()) {
          String currentLine = itr.next();
          
          if(isWordInLine("<path", currentLine)) {
              System.out.println("\n\n*Found Path on line " + currentLineNumber);
              
              finalPointList = extractPointsFromPointString(extractPointDataFromPathStartAndPathEndPositions(l, currentLineNumber, findPathEndingLine(l, currentLineNumber)));
          }
          //System.out.println(currentLine);
          currentLineNumber++;
      }
      
      String ArduinoPort = "COM6";
      int BAUD_RATE = 9600;
      Arduino arduino = new Arduino(ArduinoPort, BAUD_RATE);
      
      arduino.openConnection();
      
      for(int i = 0; i < finalPointList.size(); i++) {
          try {
              arduino.serialWrite(finalPointList.get(i));
              System.out.println(finalPointList.get(i) + " added to serial");
          }
          catch(Exception e)  {
              System.out.println("Error In Sending to serial port: " + e);
          }
      }
      arduino.closeConnection();
  }
}