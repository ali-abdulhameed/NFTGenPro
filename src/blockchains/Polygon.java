package blockchains;

import standards.ERC721;

public class Polygon extends ERC721 implements Blockchain{
	private static Polygon instance;
	static {
		instance = new Polygon();
	}
	
	private Polygon() {
		super("Polygon");
	}
	
	public static Polygon getInstance() {
		return instance;
	}
}
