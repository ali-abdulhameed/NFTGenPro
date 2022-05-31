package views;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

public class MainView extends JFrame {

	public static final int WIDTH, HEIGHT;
	private final static MainView instance;
	static {
		HEIGHT = 600;
		WIDTH = 500;
		instance = new MainView();
	}

	private MainView() {
		setTitle("NFTGenPro");
		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		JTabbedPane pane = new JTabbedPane();
		pane.add(GeneratorView.getInstance(), "Generate");
		pane.add(UpdaterView.getInstance(), "Update");
		pane.add(AboutView.getInstance(), "About");
		pane.setForeground(Color.black);
		getContentPane().add(pane);
		pack();
		setVisible(true);
	}

	public static MainView getInstance() {
		return instance;
	}
}
