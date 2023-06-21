package kmk.KSP.Kraft;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import kmk.KSP.parse.Element;
import kmk.KSP.parse.Lex;

/*  Read a craft file and identify every partloc and its subordinates.
    Display the assembly tree.
*/

public class Kraft {

	static boolean num = true;
	static int id = 0;
	static String version = "1.3.1.3";
	
  static Hashtable<String, Part>	parts = new Hashtable<String, Part>();

  static String get_args(String[] args) {
  	if (args.length == 0) return null;
  	int argx = 0;
  	if (args[argx].equals("--diffs")) {
  		num = false;
  		argx++;
  	}
  	if (argx >= args.length) return null;
  	return args[argx];
  }
  
  public static void main(String[] args) throws IOException, URISyntaxException
  {
  	System.out.println("Kraft " + version);
  	URL loc = Kraft.class.getProtectionDomain().getCodeSource().getLocation();
  	String path = Paths.get(new File(loc.toURI()).getPath().toString(), "..").normalize().toString();
  	if (path.indexOf("GameData") < 0) {
  		System.out.println("must be located as ./GameData/Kraft/Kraft.jar");
  		path = "/home/kmk/env/games/KSP/1.3.1.1891/";
  	} else {
  		path = new File(Paths.get(path, "../..").normalize().toString()).getPath().toString();
  	}
    //String craft = "saves/Lab/Ships/VAB/Jack Sprat.craft";
    String response = get_args(args);
  	BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    if (response == null) {
    	System.out.print("Enter abbrev for craft: ");
    	response = br.readLine();
    }
    if (response.length() == 0) return;
  	List<File> list = scan_craft(path, response);
  	if (list.size() == 0) {
  		System.out.println("No craft with name (abbreviated) " + response);
  		return;
  	}
  	File craft = list.get(0);
  	if (list.size() > 1) {
  		int choice = 0;
  		for (File file : list) {
  			System.out.println(choice++ + ". " + file.getPath());
  		}
  		System.out.print("Select a craft: ");
  		try {
  			choice = Integer.parseInt(br.readLine());
  		} catch (NumberFormatException exc) {
  			return;
  		}
  		if (choice < 0 || choice >= list.size()) System.exit(0);
  		craft = list.get(choice);
  	}
    path = craft.toString();
  	System.out.println("Reading " + path);

    /*  Parse the .craft file placing the Parts into a Map.
    */
  	Lex lex = new Lex(path);
    Element.Group root = new Element.Group();
    root.elements(lex);
    
    Part rootpart = null;
    HashMap parts = new HashMap<String, Part>();
    for (Element part : root.values) {
    	if (part.name.equals("PART")) {
    		// add the part to a dictionary
    		String name = part.find("part").value();
    		Part p = new Part(name);
    		parts.put(name, p);
    		if (rootpart == null) rootpart = p;
    	}
    }

    /*  Link the Parts to each other as specified.
    */
    for (Element part : root.values) {
    	if (part.name.equals("PART")) {
    		String name = part.find("part").value();
    		Part linkfrom = (Part)parts.get(name);
    		for (Element link : ((Element.Group)part).values) {
    			if (link.name.equals("link")) {
    				// lookup the linked part in the dictionary;
    				name = link.value();
    				Part linkto = (Part)parts.get(name);
    				linkfrom.links.put(name, linkto);
    				linkto.nrefs++;
    			}
    		}
    	}
    }

    /*  Identify the root part.  It appears to be the Part
     		that is not referred to by others!
    */
    rootpart = null;
    for (Object pit : parts.values()) {
    	Part part = (Part)pit;
    	if (part.nrefs == 0) {
    		if (rootpart == null) rootpart = part;
    		else System.out.println("orphan part?  " + part.name);
    	}
    }
    
    /* recursively list parts in tree order from the root
    */
    parts_display(rootpart, 0, 0);
  }

  static void parts_display(Part part, int parent, int depth)
  {
  	part.id = ++id;
  	if (num) System.out.print(String.format("%4d %4d ", part.id, parent));
    String format = "%" + (depth * 2 + 1) + "s %s";
    System.out.println(String.format(format, " ", part.name));
    depth++;
    for (Entry<String, Part> subpart : part.links.entrySet())
    {
      parts_display(subpart.getValue(), part.id, depth);
    }
  }
  
  static List<File> scan_craft(String path, String abbrev) {
  	abbrev = abbrev.toLowerCase();
  	File file = new File(path);
  	ArrayList<File> files = new ArrayList<File>();
  	return scan_craft(files, file, abbrev);
  }
  
  static List<File> scan_craft(List<File> files, File file, String abbrev) {
  	String name = file.getName().toLowerCase();
  	int ext = name.lastIndexOf(".");
  	if (ext >= 0) {
  		if (name.substring(ext).equals(".craft")) {
  			name = name.substring(0,  ext);
  	  	if (name.indexOf(abbrev) >= 0) files.add(file);
  		}
  	}
    File[] children = file.listFiles();
    if (children != null) {
      for (File child : children) {
      	scan_craft(files, child, abbrev);
      }
    }
    return files;
  }
}
