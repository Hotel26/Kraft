package kmk.KSP.parse;

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.io.PrintStream;

public abstract class Element {
  public String name;
  int depth;		// do we need?

  static String spaces =
      "                                                                " +
      "                                                                " +
      "                                                                " +
      "                                                                " +
      "                                                                " +
      "                                                                " +
      "                                                                " +
      "                                                                " +
      "                                                                " +
      "                                                                " +
      "                                                                ";
    
  static String tabs = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" +
  	"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t" +
   	"\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t";
      
  public static class Attribute extends Element {
    String value;
    
    protected Attribute(String name, String value, int depth) {
    	super(name, depth);
      this.value = value;
    }
    
    public void dump() {
      System.out.println(name + " = " + value);
    }

    public void save(PrintStream out, int depth) {
      out.println(indent(depth) + name + " = " + value);
    }
  }

  protected Element(String name, int depth) {
    this.name = name;
    this.depth = depth;
  }

  public static class Group extends Element {
    public List<Element> values = new ArrayList<Element>();
    
    public Group() { super(null, 0); }
    
    public Group(String name, int depth) { super(name, depth); }
    
    public Group elements(Lex lex) throws IOException {
   	 do {
   		 lex.line();
   		 if (lex.ch == 'a') {		// name
   			 Element elt;
   			 if (lex.value != null) {	// attribute
   				 elt = new Element.Attribute(lex.name, lex.value, depth);
   				 //System.out.println("adding attr " + elt.name);
   			 } else {			// group
   				 //System.out.println("adding group " + lex.name + " @ " + (depth + 1));
   				 elt = new Group(lex.name, depth + 1).elements(lex);
   				 if (lex.ch != '}') {
   					 System.out.println(elt.name + " expected } at line " + lex.lino);
   					 System.exit(0);
   				 }
   			 }
   			 values.add(elt);
   		 } else if (lex.ch == '}' || lex.ch == 0) {
   			 return this;
   		 }
   	 } while (true);
    }
    
    public Element find(String name) {
    	for (Element elt : values) {
    		if (elt.name.equals(name)) return elt;
    	}
    	return null;
    }

    public void save(PrintStream out, int depth) {
    	Attribute sit = (Attribute)find("sit");
    	if (sit != null && sit.value.equals("VAPORIZE")) return;
      out.println(indent(depth) + name);
      out.println(indent(depth) + "{");
      for (Element elt : values) {
      	//out.println(indent(depth + 1) + elt.name);
      	elt.save(out, depth + 1);
      }
      out.println(indent(depth) + "}");
    }
  }

	public Element find(String string) {
		return ((Group)this).find(name);
	}

  public boolean name(String name) {
  	return this.name.equals(name);
  }
  
  public double number() {
  	return Double.parseDouble(((Attribute)this).value);  	
  }
  
  public abstract void save(PrintStream out, int depth);
  
  public String value() {
  	return ((Attribute)this).value;
  }
  
  public boolean value(String value) {
  	return ((Attribute)this).value.equals(value);  	
  }  
    
  static String indent(int depth) {
  	return tabs.substring(0, depth);
  }
}
