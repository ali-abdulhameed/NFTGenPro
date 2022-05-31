package blockchains;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import main.java.org.json.JSONObject;
import standards.ERC721;
import controllers.GeneratorController;
import controllers.GeneratorController.GeneratorSession;
import controllers.UpdaterController;
import controllers.UpdaterController.UpdaterSession;
import utils.Errors;
import utils.FileManagement;
import utils.ViewSection;

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
