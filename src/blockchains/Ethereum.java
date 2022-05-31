package blockchains;

import standards.ERC721;

public class Ethereum extends ERC721 implements Blockchain {
	private static Ethereum instance;
	static {
		instance = new Ethereum();
	}
	
	private Ethereum() {
		super("Ethereum");
	}
	
	public static Ethereum getInstance() {
		return instance;
	}
}
