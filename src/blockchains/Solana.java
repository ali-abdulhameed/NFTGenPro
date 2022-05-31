package blockchains;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
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
import static utils.FileManagement.isDigit;
import utils.ViewSection;

public class Solana implements Blockchain {
	private static Solana instance;
	private String blockchainName;
	private ViewSection generatorMetaFields;
	private ViewSection updaterMetaFields;
	static {
		instance = new Solana();
	}

	private Solana() {
		blockchainName = "Solana";
	}

	public static Solana getInstance() {
		return instance;
	}

	@Override
	public String getName() {
		return blockchainName;
	}

	@Override
	public ViewSection getGeneratorMetaFields() {
		if (generatorMetaFields != null)
			return generatorMetaFields;
		generatorMetaFields = new ViewSection();
		JPanel panel = generatorMetaFields.getPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JTextField collectionName = new JTextField("Collection Name");
		JCheckBox includeName = new JCheckBox("Include name in NFTs?", false);
		JTextField symbol = new JTextField("Symbol");
		JTextField desc = new JTextField("Description");
		JTextField sellerFeeBasis = new JTextField("Seller Fee Basis Points");
		JTextField externalURL = new JTextField("External url");
		ViewSection collection = new ViewSection();
		JPanel collectionPanel = collection.getPanel();
		collectionPanel.setLayout(new BoxLayout(collectionPanel, BoxLayout.Y_AXIS));
		JCheckBox collectionToggler = new JCheckBox("Collection?");
		collection.addComponent("collectionToggler", collectionToggler, true);
		JTextField name = new JTextField("Name");
		name.setAlignmentX(Component.LEFT_ALIGNMENT);
		JTextField family = new JTextField("Family");
		family.setAlignmentX(Component.LEFT_ALIGNMENT);
		collectionToggler.addActionListener(e -> {
			JCheckBox c = (JCheckBox) e.getSource();
			if (c.isSelected()) {
				collection.addComponent("name", name, true);
				collection.addComponent("family", family, true);
			} else {
				collection.removeComponent(name);
				collection.removeComponent(family);
			}
		});
		ViewSection creators = new ViewSection();
		JPanel creatorsPanel = creators.getPanel();
		creatorsPanel.setLayout(new BoxLayout(creatorsPanel, BoxLayout.Y_AXIS));
		JCheckBox creatorsToggler = new JCheckBox("Creators?");
		creators.addComponent("creatorsToggler", creatorsToggler, true);
		JButton addCreator = new JButton("Add Creator");
		ViewSection creatorsList = new ViewSection();
		JPanel creatorsListPanel = creatorsList.getPanel();
		creatorsListPanel.setLayout(new BoxLayout(creatorsListPanel, BoxLayout.Y_AXIS));
		creators.addComponent("creatorsList", creatorsList, false);
		creatorsToggler.addActionListener(e -> {
			JCheckBox c = (JCheckBox) e.getSource();
			if (c.isSelected()) {
				creators.addComponent(null, addCreator, true);
				creators.addComponent(null, creatorsListPanel, true);
			} else {
				creators.removeComponent(addCreator);
				creators.removeComponent(creatorsListPanel);
			}
		});
		addCreator.addActionListener(e -> {
			ViewSection creator = new ViewSection();
			JPanel creatorPanel = creator.getPanel();
			creatorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			creatorPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
			JTextField address = new JTextField("Address");
			JTextField share = new JTextField("Share");
			creator.addComponent("address", address, true);
			creator.addComponent("share", share, true);
			JButton remove = new JButton("x");
			remove.setForeground(Color.RED);
			remove.addActionListener(e2 -> {
				creatorsList.getComponents().remove(creator.hashCode() + "");
				creatorsList.removeComponent(creator.getPanel());
			});
			creatorsList.addComponent(null, creator.getPanel(), true);
			creatorsList.addComponent(creator.hashCode() + "", creator, false);
			creator.addComponent(null, remove, true);
		});
		generatorMetaFields.addComponent(null, Box.createVerticalStrut(5), true);
		generatorMetaFields.addComponent("collectionName", collectionName, true);
		generatorMetaFields.addComponent("includeName", includeName, true);
		generatorMetaFields.addComponent("symbol", symbol, true);
		generatorMetaFields.addComponent("desc", desc, true);
		generatorMetaFields.addComponent("sellerFeeBasis", sellerFeeBasis, true);
		generatorMetaFields.addComponent("externalURL", externalURL, true);
		generatorMetaFields.addComponent(null, collectionPanel, true);
		generatorMetaFields.addComponent("collection", collection, false);
		generatorMetaFields.addComponent(null, creatorsPanel, true);
		generatorMetaFields.addComponent("creators", creators, false);
		return generatorMetaFields;
	}

	private Map<String, Object> getGeneratorMetaData() {
		Map<String, Object> md = new HashMap<>();
		boolean includeName = generatorMetaFields.<JCheckBox>getComponent("includeName").isSelected();
		String n = generatorMetaFields.<JTextField>getComponent("collectionName").getText();
		String s = generatorMetaFields.<JTextField>getComponent("symbol").getText();
		String d = generatorMetaFields.<JTextField>getComponent("desc").getText();
		String sp = generatorMetaFields.<JTextField>getComponent("sellerFeeBasis").getText();
		String e = generatorMetaFields.<JTextField>getComponent("externalURL").getText();
		ViewSection collection = generatorMetaFields.<ViewSection>getComponent("collection");
		if (collection.<JCheckBox>getComponent("collectionToggler").isSelected()) {
			String cname = collection.<JTextField>getComponent("name").getText();
			String cfamily = collection.<JTextField>getComponent("family").getText();
			md.put("c-name", cname);
			md.put("c-family", cfamily);
		}
		ViewSection creators = generatorMetaFields.<ViewSection>getComponent("creators");
		List<Object[]> creatorsList = new LinkedList<>();
		final AtomicInteger shares = new AtomicInteger(0);
		Collection<Component> list = creators.<ViewSection>getComponent("creatorsList").getComponents().values();
		list.forEach(el -> {
			String address = ((ViewSection) el).<JTextField>getComponent("address").getText();
			String share = ((ViewSection) el).<JTextField>getComponent("share").getText();
			if (isDigit(share)) {
				shares.set(shares.get() + Integer.parseInt(share));
				creatorsList.add(new Object[] { address, Integer.parseInt(share) });
			}
		});
		n = n.contentEquals("Collection Name") ? null : n;
		s = s.contentEquals("Symbol") ? null : s;
		d = d.contentEquals("Description") ? null : d;
		sp = sp.contentEquals("Seller Fee Basis Points") ? null : sp;
		e = e.contentEquals("External url") ? null : e;
		md.put("name", n);
		md.put("symbol", s);
		md.put("description", d);

		if (sp != null && isDigit(sp)) {
			int spValue = Integer.parseInt(sp);
			if (spValue < 0 || spValue > 10000) {
				Errors.setGeneratorCustomError("\"Seller Fee Basis Points\" have to be between 0 and 10000");
			} else {
				md.put("sellerFeeBasis", spValue);
			}
		}
		boolean creatorsToggler = creators.<JCheckBox>getComponent("creatorsToggler").isSelected();
		int totalShares = shares.get();
		if (list.size() == 0 || !creatorsToggler) {
			if (md.get("sellerFeeBasis") != null) {
				Errors.setGeneratorCustomError("Error: You need to add at least one creator");
			}
			md.put("creators", null);
		} else {
			if (totalShares != 100) {
				Errors.setGeneratorCustomError("Error: creators shares need to add up to 100");
				creatorsList.clear();
			}
			md.put("creators", creatorsList);
		}
		if (md.get("creators") != null && md.get("sellerFeeBasis") == null) {
			Errors.setGeneratorCustomError("Error: You need to add \"Seller Fee Basis Points\"");
		}
		md.put("externalURL", e);
		md.put("includeName", includeName);
		return md;
	}

	@Override
	public ViewSection getUpdaterMetaFields() {
		if (updaterMetaFields != null)
			return updaterMetaFields;
		updaterMetaFields = new ViewSection();
		JPanel panel = updaterMetaFields.getPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		JTextField imageBaseURL = new JTextField("Image base url: without / at the end");
		JTextField symbol = new JTextField("Symbol");
		JTextField desc = new JTextField("Description");
		JTextField sellerFeeBasis = new JTextField("Seller Fee Basis Points");
		JTextField externalURL = new JTextField("External url");
		updaterMetaFields.addComponent("imageBaseURL", imageBaseURL, true);
		updaterMetaFields.addComponent("symbol", symbol, true);
		updaterMetaFields.addComponent("desc", desc, true);
		updaterMetaFields.addComponent("sellerFeeBasis", sellerFeeBasis, true);
		updaterMetaFields.addComponent("externalURL", externalURL, true);
		return updaterMetaFields;
	}

	@Override
	public Errors generateNFTs(GeneratorSession session) {
		try {
			List<List<File>> combinations = session.combinations;
			if (combinations.size() > 0) {
				Map<String, Object> md = getGeneratorMetaData();
				final List<Object[]> creators = (List) md.get("creators");
				if (creators != null && creators.size() == 0)
					return Errors.CustomError;
				boolean includeName = (boolean) md.get("includeName");
				final String name = (String) md.get("name");
				final String symbol = (String) md.get("symbol");
				final Integer sellerFeeBasis = (Integer) md.get("sellerFeeBasis");
				if (sellerFeeBasis == null && creators != null)
					return Errors.CustomError;
				if (sellerFeeBasis != null && creators == null)
					return Errors.CustomError;
				final String desc = (String) md.get("description");
				final String externalURL = (String) md.get("externalURL");
				final String collectionName = (String) md.get("c-name");
				final String collectionFamily = (String) md.get("c-family");
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
						metaData.put("symbol", symbol != null ? symbol : "");
						if (desc != null)
							metaData.put("description", desc);
						metaData.put("seller_fee_basis_points", sellerFeeBasis != null ? sellerFeeBasis : 0);
						metaData.put("image",
								NFTName + "." + session.outputImageType + "?ext=" + session.outputImageType);
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
							if (valid == null)
								break;
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
						if (collectionName != null && collectionFamily != null) {
							JSONObject collectionAttr = new JSONObject();
							collectionAttr.put("name", collectionName);
							collectionAttr.put("family", collectionFamily);
							metaData.put("collection", collectionAttr);
						}
						JSONObject propAttr = new JSONObject();
						JSONObject propFile = new JSONObject();
						propFile.put("uri", metaData.get("image"));
						propFile.put("type", "image/" + session.outputImageType);
						propAttr.append("files", propFile);
						propAttr.put("category", "image");
						if (creators != null) {
							creators.forEach(c -> {
								JSONObject creator = new JSONObject();
								creator.put("address", (String) c[0]);
								creator.put("share", (int) c[1]);
								propAttr.append("creators", creator);
							});
						}
						metaData.put("properties", propAttr);
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
			if (e.getMessage().endsWith(Errors.CustomError.toString()))
				return Errors.CustomError;
			return Errors.GenericError;
		}
	}

	@Override
	public Errors updateMetaData(UpdaterSession session) {
		String imageBaseURL = updaterMetaFields.<JTextField>getComponent("imageBaseURL").getText();
		String symbol = updaterMetaFields.<JTextField>getComponent("symbol").getText();
		String desc = updaterMetaFields.<JTextField>getComponent("desc").getText();
		String sellerFeeBasis = updaterMetaFields.<JTextField>getComponent("sellerFeeBasis").getText();
		final Integer sp;
		if (!sellerFeeBasis.contentEquals("Seller Fee Basis Points") && isDigit(sellerFeeBasis)) {
			int spValue = Integer.parseInt(sellerFeeBasis);
			if (spValue < 0 || spValue > 10000) {
				Errors.setUpdaterCustomError("\"Seller Fee Basis Points\" have to be between 0 and 10000");
				return Errors.CustomError;
			} else {
				sp = spValue;
			}
		} else {
			sp = null;
		}

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
					output.put("symbol", !symbol.contentEquals("Symbol") ? symbol : j.get("symbol"));
					if (!desc.contentEquals("Description")) {
						output.put("description", desc);
					} else if (j.has("description")) {
						output.put("description", j.get("description"));
					}
					if (sp != null) {
						if (!j.getJSONObject("properties").has("creators")) {
							Errors.setUpdaterCustomError(
									"Error: cannot add Seller Fee Basis Points" + " because there are no creators");
							throw new Exception(Errors.CustomError.toString());
						} else {
							output.put("seller_fee_basis_points", sp);
						}
					} else {
						output.put("seller_fee_basis_points", 0);
					}
					if (!imageBaseURL.contentEquals("Image base url: without / at the end")) {
						String image = j.getString("image");
						int i = image.lastIndexOf('/');
						image = imageBaseURL + "/" + image.substring(i == -1 ? 0 : ++i);
						output.put("image", image);
					} else {
						output.put("image", j.get("image"));
					}
					if (!externalURL.contentEquals("External url")) {
						output.put("external_url", externalURL);
					} else if (j.has("external_url")) {
						output.put("external_url", j.get("external_url"));
					}
					output.put("attributes", j.get("attributes"));
					if (j.has("collection")) {
						output.put("collection", j.get("collection"));
					}
					j.getJSONObject("properties").getJSONArray("files").getJSONObject(0).put("uri",
							output.get("image"));
					output.put("properties", j.get("properties"));

					UpdaterController.updateMetaDataFile(output, session.folderDir + "/" + file.getName());
				} catch (Exception e) {
					throw new RuntimeException(e.getMessage());
				}
				session.counter.getAndIncrement();
			});
		} catch (RuntimeException e) {
			if (e.getMessage().endsWith(Errors.CustomError.toString()))
				return Errors.CustomError;
			return Errors.GenericError;
		}
		return Errors.NoErrorDetected;
	}
}
