package vnreal.network;

public class IDSource {

	private IDSource() {
		throw new AssertionError();
	}
	
	
	static long id = 0;
	
	public static long getID() {
		if(id == Long.MAX_VALUE)
			id = 0;
		
		return id++;
	}
	
	public static void reset() {
		id = 0;
	}
	
}
