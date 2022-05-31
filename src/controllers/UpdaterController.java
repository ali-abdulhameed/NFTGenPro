package controllers;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import main.java.org.json.JSONObject;
import blockchains.Blockchain;
import utils.Errors;
import utils.FileManagement;
import views.GeneratorView;
import views.UpdaterView;

public class UpdaterController {
	
	private UpdaterController(){}
	
	public static Errors updateMetaData() {
		UpdaterView updaterView = UpdaterView.getInstance();
		String folderDir = updaterView.getFolderDir();
		if(folderDir == null) return Errors.DirectoryNotSelected;
		if(folderDir.contentEquals(Errors.InvalidDirectory.toString())) return Errors.InvalidDirectory;
		Blockchain b = MainController.getBlockchains().get(updaterView.getSelectedBlockchain());
		if(b == null) return Errors.BlockchainNotSelected;
		UpdaterSession session = new UpdaterSession(
				folderDir,
				FileManagement.listAllFiles(folderDir, "json").length
				);
		return b.updateMetaData(session);
	}
	public static void updateMetaDataFile(JSONObject metaData, String outputDirPath){
		try {
			FileWriter fw = new FileWriter(outputDirPath);
			fw.write(metaData.toString(1));
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static class UpdaterSession {
		public final String folderDir;
		public final int nOfFilesToUpdate;
		public final AtomicInteger counter;
		public volatile boolean canceled;
		UpdaterSession(
				String folderDir,
				int nOfFilesToUpdate
				){
			this.folderDir = folderDir;
			this.nOfFilesToUpdate = nOfFilesToUpdate;
			counter = new AtomicInteger(0);
			canceled = false;	
		}
		public void createProgressDialog() {
			UpdaterView.getInstance().createProgressDialog(this);
		}
	}
}
