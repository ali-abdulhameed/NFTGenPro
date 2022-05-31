package standards;

import java.awt.image.BufferedImage;
import java.io.File;
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
import javax.swing.JPanel;
import javax.swing.JTextField;
import controllers.GeneratorController;
import controllers.UpdaterController;
import controllers.GeneratorController.GeneratorSession;
import controllers.UpdaterController.UpdaterSession;
import main.java.org.json.JSONObject;
import utils.Errors;
import static utils.FileManagement.listAllFiles;
import utils.ViewSection;

public abstract class ERC721 {
	private String blockchainName;
	private ViewSection generatorMetaFields;
	private ViewSection updaterMetaFields;

	public ERC721(String blockchainName) {
		this.blockchainName = blockchainName;
	}

	public String getName() {
		return blockchainName;
	}

	public ViewSection getGeneratorMetaFields() {
		if (generatorMetaFields != null)
			return generatorMetaFields;
		generatorMetaFields = new ViewSection();
		JPanel panel = generatorMetaFields.getPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JTextField collectionName = new JTextField("Collection Name");
		JCheckBox includeName = new JCheckBox("Include name in NFTs?", false);
		JTextField desc = new JTextField("Description");
		JTextField externalURL = new JTextField("External url");
		generatorMetaFields.addComponent(null, Box.createVerticalStrut(5), true);
		generatorMetaFields.addComponent("collectionName", collectionName, true);
		generatorMetaFields.addComponent("includeName", includeName, true);
		generatorMetaFields.addComponent("desc", desc, true);
		generatorMetaFields.addComponent("externalURL", externalURL, true);
		return generatorMetaFields;
	}

	private Map<String, Object> getGeneratorMetaData() {
		Map<String, Object> md = new HashMap<>();
		boolean includeName = generatorMetaFields.<JCheckBox>getComponent("includeName").isSelected();
		String n = generatorMetaFields.<JTextField>getComponent("collectionName").getText();
		String d = generatorMetaFields.<JTextField>getComponent("desc").getText();
		String e = generatorMetaFields.<JTextField>getComponent("externalURL").getText();
		n = n.contentEquals("Collection Name") ? null : n;
		d = d.contentEquals("Description") ? null : d;
		e = e.contentEquals("External url") ? null : e;
		md.put("name", n);
		md.put("description", d);
		md.put("externalURL", e);
		md.put("includeName", includeName);
		return md;
	}

	public ViewSection getUpdaterMetaFields() {
		if (updaterMetaFields != null)
			return updaterMetaFields;
		updaterMetaFields = new ViewSection();
		JPanel panel = updaterMetaFields.getPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JTextField imageBaseURL = new JTextField("Image base url: without / at the end");
		JTextField desc = new JTextField("Description");
		JTextField externalURL = new JTextField("External url");
		updaterMetaFields.addComponent("imageBaseURL", imageBaseURL, true);
		updaterMetaFields.addComponent("desc", desc, true);
		updaterMetaFields.addComponent("externalURL", externalURL, true);
		return updaterMetaFields;
	}

	public Errors generateNFTs(GeneratorSession session) {
		try {
			List<List<File>> combinations = session.combinations;
			if (combinations.size() > 0) {
				Map<String, Object> md = getGeneratorMetaData();
				boolean includeName = (boolean) md.get("includeName");
				final String name = (String) md.get("name");
				final String desc = (String) md.get("description");
				final String externalURL = (String) md.get("externalURL");
				final String outputDirPath = session.createOutputDirectory();
				session.createProgressDialog();
				combinations.parallelStream().forEach(imgs -> {
					if (session.canceled)
						return;
					try {
						JSONObject metaData = new JSONObject();
						String NFTName = session.NFTNameCounter.incrementAndGet() + "";
						String imageName = name != null && includeName ? name + " #" + NFTName : "#" + NFTName;
						metaData.put("name", imageName);
						if (desc != null)
							metaData.put("description", desc);
						metaData.put("image", NFTName + "." + session.outputImageType);
						if (externalURL != null)
							metaData.put("external_url", externalURL);
						List<BufferedImage> imgsToCombine = new LinkedList<>();
						Integer valid = 1;
						Map<File, Integer> tempMap = new HashMap<>();
						for (File img : imgs) {
							valid = session.occurences.computeIfPresent(img, (k, v) -> {
								if (v <= 0)
									return null;
								tempMap.put(k, v);
								return --v;
							});
							if (valid == null) break;
							JSONObject attr = new JSONObject();
							String[] imgParams = img.getName().split("#");
							String imgName = imgParams[imgParams.length == 2 ? 1 : 0];
							imgsToCombine.add(ImageIO.read(img));
							attr.put("trait_type", img.getParentFile().getName().split("#")[1]);
							attr.put("value", imgName.substring(0, imgName.lastIndexOf('.')));
							metaData.append("attributes", attr);
						}
						if (valid == null) {
							session.occurences.putAll(tempMap);
							return;
						}
						GeneratorController.produceImage(imgsToCombine, outputDirPath + "/images", NFTName + "",
								session.outputImageType);
						GeneratorController.produceMetaInfoFile(metaData,
								outputDirPath + "/metadata/" + NFTName + ".json");
					} catch (Exception e) {
						throw new RuntimeException(e.getMessage());
					}
				});

			} else {
				return Errors.NoNFTLayersWereFound;
			}
			return Errors.NoErrorDetected;
		} catch (Exception e) {
			if(e.getMessage().endsWith(Errors.CustomError.toString())) return Errors.CustomError;
			return Errors.GenericError;
		}
	}

	public Errors updateMetaData(UpdaterSession session) {
		String imageBaseURL = updaterMetaFields.<JTextField>getComponent("imageBaseURL").getText();
		String desc = updaterMetaFields.<JTextField>getComponent("desc").getText();
		String externalURL = updaterMetaFields.<JTextField>getComponent("externalURL").getText();
		session.createProgressDialog();
		try {
		Arrays.asList(listAllFiles(session.folderDir, "json")).parallelStream().forEach(file -> {
			if (session.canceled)
				return;
			try {
				String fileContent = Files.readString(Path.of(file.getAbsolutePath()), StandardCharsets.UTF_8);
				JSONObject j = new JSONObject(fileContent);
				JSONObject output = new JSONObject();
				output.put("name", j.get("name"));
				if (!imageBaseURL.contentEquals("Image base url: without / at the end")) {
					String image = j.getString("image");
					int i = image.lastIndexOf('/');
					image = imageBaseURL + "/" + image.substring(i == -1 ? 0 : ++i);
					output.put("image", image);
				} else {
					output.put("image", j.get("image"));
				}
				if (!desc.contentEquals("Description")) {
					output.put("description", desc);
				} else if(j.has("description")) {
					output.put("description", j.get("description"));
				}
					
				if (!externalURL.contentEquals("External url")) {
					output.put("external_url", externalURL);
				} else if(j.has("external_url")) {
					output.put("external_url", j.get("external_url"));
				}
				output.put("attributes", j.get("attributes"));
				UpdaterController.updateMetaDataFile(output, session.folderDir + "/" + file.getName());
			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
			session.counter.getAndIncrement();
		});
		} catch(RuntimeException e) {
			if(e.getMessage().endsWith(Errors.CustomError.toString())) return Errors.CustomError;
			return Errors.GenericError;
		}
		return Errors.NoErrorDetected;
	}
}
