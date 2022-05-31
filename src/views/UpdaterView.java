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
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import controllers.MainController;
import controllers.UpdaterController;
import controllers.UpdaterController.UpdaterSession;
import utils.Errors;
import utils.FileManagement;
import utils.ViewSection;

public class UpdaterView extends JPanel {
	private static final UpdaterView instance;
	public String folderDir = null;
	private ViewSection header;
	private ViewSection metaInfo;
	private ViewSection footer;
	static {
		instance = new UpdaterView();
	}
	
	private UpdaterView() {
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
	
	private ViewSection setupHeader() {
		ViewSection section = new ViewSection();
		JPanel panel = section.getPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JButton selector = new JButton("Select Metadata folder");
		selector.setAlignmentX(Component.CENTER_ALIGNMENT);
		JLabel headerMessage = new JLabel("");
		headerMessage.setAlignmentX(Component.CENTER_ALIGNMENT);
		selector.addActionListener(e -> {
			JFileChooser foldersSelector = new JFileChooser(System.getProperty("user.home") + "/Desktop");
			foldersSelector.setDialogTitle("Select MetaData Folder");
			foldersSelector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			foldersSelector.setAcceptAllFileFilterUsed(false);
	        if (foldersSelector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
	        	File selected = foldersSelector.getSelectedFile();
	        	folderDir = selected.getAbsolutePath();
	           selector.setText(selected.getParentFile().getName() + " " + selected.getName());
	           if(selected.getName().contentEquals("metadata")) {
	        	   setHeaderMessage(FileManagement.listAllFiles(folderDir, "json").length + " file");
	           } else {
	        	   folderDir = Errors.InvalidDirectory.toString();
	           }         
	        }
		});
		section.addComponent(null, selector, true);
		section.addComponent("headerMessage", headerMessage, true);
		return section;
	}
	
	private ViewSection setupMetaInfo() {
		ViewSection section = new ViewSection();
		JPanel panel = section.getPanel();
		Component emptySpace = Box.createVerticalStrut(MainView.HEIGHT);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JPanel blockchainSelection = new JPanel();
		JPanel bsContainer = new JPanel(new BorderLayout());
		bsContainer.add(blockchainSelection, BorderLayout.NORTH);
		blockchainSelection.setLayout(new GridLayout(0,3, 0, 0));
		ButtonGroup g = new ButtonGroup();
		for(String b : MainController.getBlockchains().keySet()) {
			JRadioButton sl = new JRadioButton(b, false);
			g.add(sl);
			sl.addActionListener(e -> {
				JRadioButton bs = (JRadioButton) e.getSource();
				JPanel bMetaInfo =
						 MainController
						.getBlockchains().get(b)
						.getUpdaterMetaFields().getPanel();			
				if(bs.isSelected()) {
					section.removeComponent("updaterMetaFields");
					section.addComponent("updaterMetaFields", bMetaInfo, true);
					section.addComponent(null, emptySpace, true);
				} 
				section.refresh();
			});
			blockchainSelection.add(sl);
		}
		section.addComponent("blockchains", blockchainSelection, false);
		section.addComponent("bsContainer", bsContainer, true);
		section.addComponent(null, Box.createVerticalStrut(20), true);
		return section;
	}
	
	private ViewSection setupFooter() {
		ViewSection section = new ViewSection();
		JPanel panel = section.getPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JButton update = new JButton("Update");
		update.setAlignmentX(Component.CENTER_ALIGNMENT);
		update.addActionListener(new ActionListener() {
		    @Override
		    public void actionPerformed(ActionEvent e) {
		    	update();
		    }
		});
		section.addComponent("updateButton", update, true);
		return section;
	}
	
	private void update() {
    	SwingWorker sw = new SwingWorker() {
    		Errors status;
			@Override
			protected Object doInBackground() {	    		
				status = UpdaterController.updateMetaData();
				return null;
			}
			
			@Override
			protected void done() {
				if(status == null) return;
		   		switch(status) {
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
		   		
		   		case NoNFTLayersWereFound: {
		   			setHeaderMessage("Error: No NFT layers were found");
		   			break;
		   		}
		   		
		   		case CustomError: {
		   			setHeaderMessage(Errors.getUpdaterCustomError());
		   			break;
		   		}
		   		case GenericError: {
		   			setHeaderMessage("There was an error generating NFTs");
		   			break;
		   		}
		   		case NoErrorDetected: {
		   			setHeaderMessage("");
		   		}
		   		}
			}
    	};
    	sw.execute();
	}
	
	public String getSelectedBlockchain() {
		JPanel panel = metaInfo.<JPanel>getComponent("blockchains");
		for(Component c: panel.getComponents()) {
			if(c instanceof JRadioButton) {
				JRadioButton cb = (JRadioButton) c;
				if(cb.isSelected()) return cb.getText();
			}
		}
		return null;
	}
	public static UpdaterView getInstance() {
		return instance;
	}
	
	public String getFolderDir() {
		return folderDir;
	}
	public void setHeaderMessage(String msg) {
		JLabel headerMessage = header.<JLabel>getComponent("headerMessage");
		if(headerMessage == null) return;
		headerMessage.setText(msg);
	}
	
	public void createProgressDialog(UpdaterSession session) {
		new ProgressDialog(MainView.getInstance(), session);
	}
	
	private class ProgressDialog extends JDialog {

		ProgressDialog(JFrame frame, UpdaterSession session) {
			super(frame);
		    JButton updateButton = footer.<JButton>getComponent("updateButton");
		    updateButton.setEnabled(false);
			Point p = frame.getLocationOnScreen();
			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(e-> {
				session.canceled = true;
				cancelButton.setText("Canceled");
				cancelButton.setEnabled(false);
				updateButton.setEnabled(true);
			});
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					session.canceled = true;
					updateButton.setEnabled(true);
				}
			});
			
			JPanel labelPanel = new JPanel();
			JLabel text = new JLabel(session.counter.get() + "");
			labelPanel.add(text);
			labelPanel.add(new JLabel(" /" + session.nOfFilesToUpdate));
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
					while(
							!session.canceled &&
							session.counter.get() <= session.nOfFilesToUpdate
							) {
						try {
						text.setText(session.counter.get() + "");
						if(session.counter.get() == session.nOfFilesToUpdate) break;
						Thread.sleep(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if(!session.canceled) cancelButton.setText("Done");
					cancelButton.setEnabled(false);
					return null;
				}}.execute();
		}
	}
}
