package views;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import controllers.GeneratorController;
import controllers.MainController;
import controllers.GeneratorController.GeneratorSession;
import utils.Errors;
import utils.FileManagement;
import utils.ViewSection;

public class GeneratorView extends JPanel {
	private static final GeneratorView instance;
	public String projectDir = null;
	public ViewSection header;
	public ViewSection metaInfo;
	public ViewSection footer;
	static {
		instance = new GeneratorView();
	}

	private GeneratorView() {
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(MainView.WIDTH, MainView.HEIGHT));
		header = setupHeader();
		add(header.getPanel(), BorderLayout.NORTH);
		metaInfo = setupMetaInfo();
		JScrollPane scrollable = new JScrollPane(metaInfo.getPanel());
		scrollable.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollable.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollable.setBorder(null);
		add(scrollable, BorderLayout.CENTER);
		footer = setupFooter();
		add(footer.getPanel(), BorderLayout.SOUTH);
	}

	public static GeneratorView getInstance() {
		return instance;
	}

	private ViewSection setupHeader() {
		ViewSection view = new ViewSection();
		JPanel panel = view.getPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JButton selector = new JButton("Select project folder");
		JCheckBox rarity = new JCheckBox("Rarity", false);
		rarity.setEnabled(false);
		JLabel headerMessage = new JLabel("");
		headerMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
		selector.addActionListener(e -> {
			JFileChooser foldersSelector = new JFileChooser(System.getProperty("user.home") + "/Desktop");
			foldersSelector.setDialogTitle("Select Project Folder");
			foldersSelector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			foldersSelector.setAcceptAllFileFilterUsed(false);
			if (foldersSelector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File selected = foldersSelector.getSelectedFile();
				projectDir = selected.getAbsolutePath();
				selector.setText(selected.getName());
				if (new File(projectDir + "/layers").exists()) {
					setHeaderMessage(String.format("%,d",
							GeneratorController.nOfCombinations(rarity.isSelected() ? true : false))
							+ " possible NFTs");
					rarity.setEnabled(true);
				} else {
					projectDir = Errors.InvalidDirectory.toString();
					rarity.setEnabled(false);
				}
			}
		});

		rarity.addActionListener(e -> {
			JCheckBox r = (JCheckBox) e.getSource();
			headerMessage
					.setText(String.format("%,d", GeneratorController.nOfCombinations(r.isSelected() ? true : false))
							+ " possible NFTs");
		});
		JPanel header = new JPanel();
		header.setMaximumSize(new Dimension(MainView.WIDTH, 100));
		header.add(selector);
		header.add(rarity);
		panel.add(header);
		panel.add(headerMessage);
		view.addComponent("selectorButton", selector, false);
		view.addComponent("rarityCheckBox", rarity, false);
		view.addComponent("headerMessage", headerMessage, false);
		return view;
	}

	private ViewSection setupMetaInfo() {
		ViewSection view = new ViewSection();
		JPanel panel = view.getPanel();
		Component emptySpace = Box.createVerticalStrut(MainView.HEIGHT);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JPanel blockchainSelection = new JPanel();
		JPanel bsContainer = new JPanel(new BorderLayout());
		bsContainer.add(blockchainSelection, BorderLayout.NORTH);
		blockchainSelection.setLayout(new GridLayout(0, 3, 0, 0));
		ButtonGroup g = new ButtonGroup();
		for (String b : MainController.getBlockchains().keySet()) {
			JRadioButton sl = new JRadioButton(b, false);
			g.add(sl);
			sl.addActionListener(e -> {
				JRadioButton bs = (JRadioButton) e.getSource();
				JPanel bMetaInfo = MainController.getBlockchains().get(b).getGeneratorMetaFields().getPanel();

				if (bs.isSelected()) {
					view.removeComponent("generatorMetaFields");
					view.addComponent("generatorMetaFields", bMetaInfo, true);
					view.addComponent(null, emptySpace, true);
				}
				view.refresh();
			});
			blockchainSelection.add(sl);
		}

		view.addComponent("blockchains", blockchainSelection, false);
		view.addComponent("bsContainer", bsContainer, true);
		view.addComponent(null, Box.createVerticalStrut(20), true);
		return view;
	}

	private ViewSection setupFooter() {

		ViewSection view = new ViewSection();
		JPanel panel = view.getPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JLabel footerMessage = new JLabel();
		footerMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
		JTextField numberOfNFTs = new JTextField("#");
		numberOfNFTs.setPreferredSize(new Dimension(30, 20));
		JButton generate = new JButton("Generate");
		generate.setAlignmentX(Component.CENTER_ALIGNMENT);
		generate.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				generate();
			}
		});
		JPanel outputImgTypes = new JPanel();
		outputImgTypes.add(new JLabel("Output image type"));
		ButtonGroup g = new ButtonGroup();
		for (String t : FileManagement.IMG_OUTPUT_TYPES) {
			JRadioButton r = new JRadioButton(t, false);
			g.add(r);
			outputImgTypes.add(r);
		}
		JPanel generatePanel = new JPanel();
		generatePanel.add(numberOfNFTs);
		generatePanel.add(generate);
		view.addComponent("footerMessage", footerMessage, true);
		view.addComponent("outputImgTypes", outputImgTypes, true);
		view.addComponent(null, generatePanel, true);
		view.addComponent("numberOfNFTs", numberOfNFTs, false);
		view.addComponent("generateButton", generate, false);
		return view;
	}

	public String getProjectDir() {
		return projectDir;
	}

	public void generate() {
		SwingWorker sw = new SwingWorker() {
			Errors status;
			@Override
			protected Object doInBackground() {
				status = GeneratorController.generateNFTs();
				return null;
			}
			@Override
			protected void done() {
				if (status == null)
					return;
				switch (status) {
				case DirectoryNotSelected: {
					setHeaderMessage("Please select a directory");
					break;
				}
				case InvalidDirectory: {
					setHeaderMessage("Error: Invalid directory");
					break;
				}
				case BlockchainNotSelected: {
					setHeaderMessage("Select a blockchain");
					break;
				}
				case OutputImageTypeNotSelected: {
					setFooterMessage("Select an image output type");
					break;
				}
				
				case NoNFTLayersWereFound: {
					setHeaderMessage("Error: No NFT layers were found");
					break;
				}
				case CustomError: {
					setHeaderMessage(Errors.getGeneratorCustomError());
					break;
				}
				case GenericError: {
					setHeaderMessage("There was an error generating NFTs");
					break;
				}
				case NoErrorDetected: {
					setHeaderMessage("");
					setFooterMessage("");
				}
				}
			}
		};
		sw.execute();
	}

	public void setHeaderMessage(String msg) {
		JLabel headerMessage = header.<JLabel>getComponent("headerMessage");
		if (headerMessage == null)
			return;
		headerMessage.setText(msg);
	}

	public void setFooterMessage(String msg) {
		JLabel footerMessage = footer.<JLabel>getComponent("footerMessage");
		if (footerMessage == null)
			return;
		footerMessage.setText(msg);
	}

	public int getNumberOfNFTsToGenerate() {
		JTextField input = footer.<JTextField>getComponent("numberOfNFTs");
		boolean isNumber = input.getText().chars().allMatch(Character::isDigit);
		if (isNumber && input.getText().length() != 0)
			return Integer.parseInt(input.getText());
		return -1;
	}

	public String getSelectedBlockchain() {
		JPanel panel = metaInfo.<JPanel>getComponent("blockchains");
		for (Component c : panel.getComponents()) {
			if (c instanceof JRadioButton) {
				JRadioButton cb = (JRadioButton) c;
				if (cb.isSelected())
					return cb.getText();
			}
		}
		return null;
	}

	public boolean rarityEnabled() {
		return header.<JCheckBox>getComponent("rarityCheckBox").isSelected();
	}

	public String getSelectedOutputImageType() {
		for (Component c : footer.<JPanel>getComponent("outputImgTypes").getComponents()) {
			if (c instanceof JRadioButton) {
				JRadioButton b = (JRadioButton) c;
				if (b.isSelected())
					return b.getText().toLowerCase();
			}
		}
		return null;
	}

	public void createProgressDialog(GeneratorSession session) {
		new ProgressDialog(MainView.getInstance(), session);
	}

	private class ProgressDialog extends JDialog {

		ProgressDialog(JFrame frame, GeneratorSession session) {
			super(frame);
			JButton generateButton = footer.<JButton>getComponent("generateButton");
			generateButton.setEnabled(false);
			Point p = frame.getLocationOnScreen();
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(e -> {
				session.canceled = true;
				cancelButton.setText("Canceled");
				cancelButton.setEnabled(false);
				generateButton.setEnabled(true);
			});
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					session.canceled = true;
					generateButton.setEnabled(true);
				}
			});
			JPanel labelPanel = new JPanel();
			JLabel text = new JLabel("0");
			labelPanel.add(text);
			labelPanel.add(new JLabel(" /" + session.nOfNFTsToGenerate));
			cancelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
			labelPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.add(Box.createVerticalStrut(50));
			panel.add(Box.createHorizontalStrut(200));
			panel.add(cancelButton);
			panel.add(labelPanel);
			panel.add(Box.createVerticalStrut(50));
			add(panel);
			pack();
			setLocation(p.x - (WIDTH - getWidth()) / 2, p.y - (HEIGHT - getHeight()) / 2);
			setVisible(true);
			new SwingWorker() {

				@Override
				protected Object doInBackground() throws Exception {
					while (!session.canceled && session.NFTNameCounter.get() <= session.nOfNFTsToGenerate) {
						try {
							int value = session.NFTNameCounter.get();
							text.setText(value + "");
							if (value == session.nOfNFTsToGenerate)
								break;
							Thread.sleep(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (session.NFTNameCounter.get() == session.nOfNFTsToGenerate) {
						cancelButton.setText("Done");
						cancelButton.setEnabled(false);
					}
					return null;
				}
			}.execute();
		}
	}
}
