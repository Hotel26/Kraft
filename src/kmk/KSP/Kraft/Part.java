package kmk.KSP.Kraft;

import java.util.HashMap;

public class Part
{
	public int			id;
  public String		name;
  public Part		parent;		// unused?
  public int nrefs;				// # links to this part
  public HashMap<String, Part> links;
  
  Part(String name) {
    this.name = name;
    links = new HashMap<String, Part>();
  }
}
