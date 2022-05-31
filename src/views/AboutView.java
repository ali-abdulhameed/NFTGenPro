package views;

import java.awt.Cursor;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class AboutView extends JPanel {
	private static AboutView instance;
	static {
		instance = new AboutView();
	}
	
	private AboutView() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JTextArea about = new JTextArea("This app was created by: @0xAliA\n"
				+ "For documentation visit github.com/ali-abdulhameed/NFTGenPro\n\n"
				+ "Buy me coffee/ send me an NFT?\n\n" + "Cashapp: $0xAliA\n"
				+ "Bitcoin: bc1qc4f22qvj3m5wzquaw9qyvyvtsvskx500tm8tas\n"
				+ "Ethereum/ERC20: 0x8F2ACB07f8C9dFEbfd0Bab02A408f11f1d3eF191\n");
		about.setBackground(null);
		about.setEditable(false);
		add(about);
		add(Box.createVerticalStrut(MainView.HEIGHT));
	}

	public static AboutView getInstance() {
		return instance;
	}
}
