//Project Built By Jonathan Warner

import java.util.*; 
import java.nio.charset.StandardCharsets; 
import java.nio.file.*; 
import java.io.*; 

public class SvgWriter
{
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
  
  public static List<String> extractPointsFromPath(String pathString) {
      List<String> points = new ArrayList<String>(); 
      
      for(int i = 0; i < pathString.length(); i++) {
           
      }
      
      
      return points;
  }
  
  public static void main(String[] args) 
  { 
    List l = readFileInList("./test.svg"); 
    boolean foundPath = false;
    boolean foundPointList = false;
    String pointList = "";
    
    int pointListStart = -1;
    int pointListEnd = -1;
    
    Iterator<String> itr = l.iterator(); 
    while (itr.hasNext()) {
      String currentLine = itr.next();
      //System.out.println(currentLine);
      if(currentLine.length() > 5) {    
          for(int i = 0; i < currentLine.length()-4; i++) {
              if(currentLine.substring(i, i+5).equals("<path")) {
                  foundPath = true;
                  extractPointsFromPath(currentLine);
              }
              if(foundPath && currentLine.substring(i+2, i+5).equals("d=\"")) {
               foundPointList = true; 
               pointListStart = i+5;
              }
               if(foundPointList && currentLine.substring(i+4, i+5).equals("\"")) {
               foundPointList = false;
               pointListEnd = i+4;
              }
          }
          if(pointListStart != -1 && pointListEnd != -1) {
            System.out.println(currentLine.substring(pointListStart, pointListEnd));
            pointListStart = 0;
            pointListEnd = 0;
          }
      }
      //System.out.println(currentLine);
    }
  }
}

