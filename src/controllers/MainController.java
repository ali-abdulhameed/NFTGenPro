package controllers;

import java.util.LinkedHashMap;
import java.util.Map;
import blockchains.Binance;
import blockchains.Blockchain;
import blockchains.Ethereum;
import blockchains.Polygon;
import blockchains.Solana;

public class MainController {
	
	public static Map<String, Blockchain> blockchains;
	static {
		setupBlockchains();
	}
	private MainController() {}
	
	public static Map<String, Blockchain> getBlockchains() {
		return blockchains;
	}
	
	private static void setupBlockchains() {
		blockchains =  new LinkedHashMap<>();
		Ethereum ethereum = Ethereum.getInstance();
		Polygon polygon = Polygon.getInstance();
		Binance binance = Binance.getInstance();
		Solana solana = Solana.getInstance();
		blockchains.put(ethereum.getName(), ethereum);
		blockchains.put(polygon.getName(), polygon);
		blockchains.put(binance.getName(), binance);
		blockchains.put(solana.getName(), solana);
	}
		
}
