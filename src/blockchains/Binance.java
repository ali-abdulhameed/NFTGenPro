package blockchains;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import controllers.GeneratorController;
import controllers.GeneratorController.GeneratorSession;
import main.java.org.json.JSONObject;
import standards.ERC721;
import utils.Errors;
import utils.ViewSection;
import static utils.FileManagement.isDigit;

public class Binance extends ERC721 implements Blockchain {
	private static Binance instance;
	static {
		instance = new Binance();
	}
	
	private Binance() {
		super("Binance");
		extendGeneratorView();
	}
	
	public static Binance getInstance() {
		return instance;
	}

	private void extendGeneratorView() {
		ViewSection numericalTraits = new ViewSection();
		JPanel numericalTraitsPanel = numericalTraits.getPanel();
		numericalTraitsPanel.setLayout(new BoxLayout(numericalTraitsPanel, BoxLayout.Y_AXIS));
		JCheckBox numericalTraitsToggler = new JCheckBox("Numerical Traits?");
		JButton createTrait = new JButton("Create Numerical Trait");
		ViewSection traitsList = new ViewSection();
		JPanel traitsPanel = traitsList.getPanel();
		traitsPanel.setLayout(new BoxLayout(traitsPanel, BoxLayout.Y_AXIS));
		JLabel note = new JLabel("Only change Min field for traits with single value");
		numericalTraitsToggler.addActionListener(e -> {
			JCheckBox c = (JCheckBox) e.getSource();
			if (c.isSelected()) {
				numericalTraits.addComponent(null, createTrait, true);
				numericalTraits.addComponent(null, note, true);
				numericalTraits.addComponent(null, traitsPanel, true);
			} else {
				numericalTraits.removeComponent(createTrait);
				numericalTraits.removeComponent(note);
				numericalTraits.removeComponent(traitsPanel);
			}
		});
		createTrait.addActionListener(e -> {
			ViewSection trait = new ViewSection();
			JPanel traitPanel = trait.getPanel();
			traitPanel.setLayout(new BoxLayout(traitPanel, BoxLayout.Y_AXIS));
			traitPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

			JPanel panel1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			JTextField traitType = new JTextField("Trait Type");
			JComboBox<String> displayType = new JComboBox<>(
					new String[] { "number", "boost_number", "boost_percentage" });
			JButton remove = new JButton("x");
			remove.setForeground(Color.RED);
			remove.addActionListener(e2 -> {
				traitsList.getComponents().remove(trait.hashCode() + "");
				traitsList.removeComponent(trait.getPanel());
			});
			panel1.add(traitType);
			panel1.add(displayType);
			panel1.add(remove);
			JPanel panel2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
			ButtonGroup g = new ButtonGroup();
			JRadioButton all = new JRadioButton("all", true);
			JRadioButton randomized = new JRadioButton("randomized");
			g.add(all);
			g.add(randomized);
			JTextField min = new JTextField("min");
			JTextField max = new JTextField("max");
			panel2.add(all);
			panel2.add(randomized);
			panel2.add(min);
			panel2.add(max);
			trait.addComponent(null, panel1, true);
			trait.addComponent(null, panel2, true);
			trait.addComponent("traitType", traitType, false);
			trait.addComponent("displayType", displayType, false);
			trait.addComponent("all", all, false);
			trait.addComponent("randomized", randomized, false);
			trait.addComponent("min", min, false);
			trait.addComponent("max", max, false);
			traitsList.addComponent(null, traitPanel, true);
			traitsList.addComponent(trait.hashCode() + "", trait, false);
		});
		ViewSection view = getGeneratorMetaFields();
		view.addComponent(null, numericalTraitsPanel, true);
		view.addComponent("numericalTraits", numericalTraits, false);
		numericalTraits.addComponent("numericalTraitsToggler", numericalTraitsToggler, true);
		numericalTraits.addComponent("traitsList", traitsList, false);
		numericalTraits.addComponent(null, traitsPanel, true);
	}

	private Map<String, Object> getGeneratorMetaData() {
		ViewSection generatorMetaFields = getGeneratorMetaFields();
		Map<String, Object> md = new HashMap<>();
		boolean includeName = generatorMetaFields.<JCheckBox>getComponent("includeName").isSelected();
		String n = generatorMetaFields.<JTextField>getComponent("collectionName").getText();
		String d = generatorMetaFields.<JTextField>getComponent("desc").getText();
		String e = generatorMetaFields.<JTextField>getComponent("externalURL").getText();
		ViewSection numericalTraits = generatorMetaFields.<ViewSection>getComponent("numericalTraits");
		ViewSection traitsList = numericalTraits.<ViewSection>getComponent("traitsList");
		List<Map<String, Object>> traits = new LinkedList<>();
		Collection<Component> tList = traitsList.getComponents().values();
		tList.forEach(trait -> {
			ViewSection t = (ViewSection) trait;
			Map<String, Object> tm = new HashMap<>();
			String traitType = t.<JTextField>getComponent("traitType").getText();
			String displayType = (String) t.<JComboBox>getComponent("displayType").getSelectedItem();
			JRadioButton all = t.<JRadioButton>getComponent("all");
			JRadioButton randomized = t.<JRadioButton>getComponent("randomized");
			String min = t.<JTextField>getComponent("min").getText();
			String max = t.<JTextField>getComponent("max").getText();

			if (isDigit(min)) {
				tm.put("min", Integer.parseInt(min));
			} else {
				return;
			}
			if (isDigit(max)) {
				int maxValue = Integer.parseInt(max);
				if (maxValue < (int) tm.get("min"))
					return;
				tm.put("max", maxValue);
			}
			tm.put("selected", all.isSelected() ? "all" : "randomized");
			tm.put("traitType", traitType);
			tm.put("displayType", displayType);
			traits.add(tm);
		});
		
		System.out.println(traits.size());
		n = n.contentEquals("Collection Name") ? null : n;
		d = d.contentEquals("Description") ? null : d;
		e = e.contentEquals("External url") ? null : e;
		md.put("name", n);
		md.put("description", d);
		md.put("externalURL", e);
		md.put("includeName", includeName);
		md.put("numericalTraits", tList.size() == 0 ? null: traits);
		return md;
	}

	@Override
	public Errors generateNFTs(GeneratorSession session) {
		try {
			List<List<File>> combinations = session.combinations;
			if (combinations.size() > 0) {
				Map<String, Object> md = getGeneratorMetaData();
				boolean includeName = (boolean) md.get("includeName");
				final String name = (String) md.get("name");
				final String desc = (String) md.get("description");
				final String externalURL = (String) md.get("externalURL");
				final List<Map<String, Object>> numericalTraits = (List) md.get("numericalTraits");
				if(numericalTraits.size() == 0) {
					Errors.setGeneratorCustomError("Error: Invalid numerical value");
					throw new RuntimeException(Errors.CustomError.toString());
				}
				final String outputDirPath = session.createOutputDirectory();
				SecureRandom rand = new SecureRandom();
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
						if (numericalTraits != null) {
							numericalTraits.forEach(trait -> {
								JSONObject attr2 = new JSONObject();
								String displayType = (String) trait.get("displayType");
								String traitType = (String) trait.get("traitType");
								String selected = (String) trait.get("selected");
								int min = (int) trait.get("min");
								Integer max = (Integer) trait.get("max");
								attr2.put("display_type", displayType);
								attr2.put("trait_type", traitType);
								switch (selected) {

								case "all": {
									attr2.put("value", (max != null ? rand.nextInt(max - min) + min : min) + "");
									metaData.append("attributes", attr2);
									break;
								}
								case "randomized": {
									if (rand.nextInt(session.nOfNFTsToGenerate) % 2 == 0) {
										attr2.put("value", (max != null ? rand.nextInt(max - min) + min : min) + "");
										metaData.append("attributes", attr2);
									}
								}
								}
							});
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
		} catch (RuntimeException e) {
			if (e.getMessage().endsWith(Errors.CustomError.toString())) return Errors.CustomError;
			return Errors.GenericError;
		}
	}
}
