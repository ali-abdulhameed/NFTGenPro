package blockchains;

import controllers.GeneratorController.GeneratorSession;
import controllers.UpdaterController.UpdaterSession;
import utils.Errors;
import utils.ViewSection;

public interface Blockchain {
	public String getName();
	public ViewSection getGeneratorMetaFields();
	public ViewSection getUpdaterMetaFields();
	public Errors generateNFTs(GeneratorSession session);
	public Errors updateMetaData(UpdaterSession session);
}
