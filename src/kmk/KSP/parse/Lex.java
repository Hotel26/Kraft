package kmk.KSP.parse;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Lex {
  String		path;
  BufferedReader	file;
  String		line;
  
  public int		lino;
  public char		ch;
  public String		name;
  public String		value;
  
  public Lex(String path) throws FileNotFoundException {
    file = new BufferedReader(new FileReader(path));
  }
  
  public boolean line() throws IOException {
    line = file.readLine();
    if (line != null) {
      lino++;
      //System.out.println(lino + ": " + line);
      // parse the line for ch, name and value
      int chx = 0;
      while ((ch = line.charAt(chx)) == ' ' || ch == '\t') chx++;
      /// filter out blank lines
      if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') {
      	ch = 'a';
      	String[] split = line.substring(chx).split(" =");
      	name = split[0];
      	value = split.length > 1 ? split[1].substring(1) : null; 
      }
      return true;
    } else {
      ch = 0;	// indicate eof
      return false;
    }
  }
}
