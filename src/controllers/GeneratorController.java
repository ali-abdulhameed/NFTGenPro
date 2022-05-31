package controllers;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import main.java.org.json.JSONObject;
import com.google.common.collect.Lists;
import blockchains.Blockchain;
import utils.Errors;
import utils.FileManagement;
import views.GeneratorView;

public class GeneratorController {

	private GeneratorController() {}
	
	public static Errors generateNFTs() {
		GeneratorView generatorView = GeneratorView.getInstance();
		String projectDir = generatorView.getProjectDir();
		if(projectDir == null) return Errors.DirectoryNotSelected;
		if(projectDir.contentEquals(Errors.InvalidDirectory.toString())) return Errors.InvalidDirectory;
		Blockchain b = MainController.getBlockchains().get(generatorView.getSelectedBlockchain());
		if(b == null) return Errors.BlockchainNotSelected;
		String outputImgType = generatorView.getSelectedOutputImageType();
		if(outputImgType == null) return Errors.OutputImageTypeNotSelected;
		GeneratorSession session = GeneratorController.generateCombinations(
				generatorView.rarityEnabled(),
				projectDir,
				b.getName(),
				outputImgType);
		if(session == null) return Errors.GenericError;
		return b.generateNFTs(session);
	}
	
	private static GeneratorSession generateCombinations(boolean rarity, String projectDir, String blockchain, String outputImgType) {
		GeneratorView generatorView = GeneratorView.getInstance();
		File[] layersFolders = FileManagement.listAllFiles(generatorView.getProjectDir() + "/layers", "dir");
		List<List<File>> layersCombo = new LinkedList<>();
		ConcurrentHashMap<File, Integer> rarityHelper = new ConcurrentHashMap<>();
		int totalCombs = nOfCombinations(false);
		Arrays.sort(layersFolders);
		for(File f : layersFolders) {
			List<File> content = new LinkedList<>();
			File[] imgs = FileManagement.listAllFiles(f.getAbsolutePath(), "img");
			int occurences = totalCombs / imgs.length;
			for(File img : imgs) {
				String[] params = img.getName().split("#");
				content.add(img);
				if(rarity && params.length == 2) {
						float occurPer = Float.parseFloat(params[0]) / 100f;
						rarityHelper.put(img, (int) (occurences * occurPer));
				} else {
					rarityHelper.put(img, occurences);
				}
			}
			layersCombo.add(content);
		}
		int nOfNFTsToGenerate = generatorView.getNumberOfNFTsToGenerate();
		int nOfCombinations = nOfCombinations(rarity);
		List<List<File>> combinations = Lists.cartesianProduct(layersCombo);
		combinations = new LinkedList(combinations);
		Collections.shuffle(combinations);
		if(nOfNFTsToGenerate <= 0 || nOfNFTsToGenerate > nOfCombinations) {
			nOfNFTsToGenerate = nOfCombinations;
		}
		return new GeneratorSession(
				projectDir,
				blockchain,
				nOfNFTsToGenerate,
				combinations.subList(0, nOfNFTsToGenerate),
				rarityHelper,
				outputImgType);
	}
	
	public static int nOfCombinations(boolean rarity) {
		String projectDir = GeneratorView.getInstance().getProjectDir();
        float totalCount = 1;
        for(File file : FileManagement.listAllFiles(projectDir + "/layers", "dir")) {
        	if(rarity) {
        		float layerCount = 0;
        		for(String img: file.list()) {
        			if(!FileManagement.isImage(img)) continue;
        			String[] name = img.split("#");
        			if(name.length == 2) {
        				layerCount += Float.parseFloat(name[0]) / 100f;
        			} else {
        				layerCount += 1;
        			}
        		}
        		totalCount *= layerCount;
        	} else {
        		totalCount *= FileManagement.listAllFiles(projectDir + "/layers/" + file.getName(), "img").length;
        	}
        }
        return (int) totalCount;
	}
	
	public static void produceImage(List<BufferedImage> imgs, String outputDirPath, String name, String outputImageType) throws Exception {
		BufferedImage combined = new BufferedImage(imgs.get(0).getWidth(), imgs.get(0).getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics g = combined.getGraphics();
		for(BufferedImage im : imgs) {
			g.drawImage(im, 0, 0, null);
		}
		g.dispose();
		ImageWriter writer = ImageIO.getImageWritersByFormatName(outputImageType).next();
		ImageWriteParam writeParam = writer.getDefaultWriteParam();
		writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		writeParam.setCompressionQuality(1.0f);
		FileImageOutputStream outputStream = new FileImageOutputStream(new File(outputDirPath + "/" + name + "." + outputImageType));
		writer.setOutput(outputStream);
		IIOImage outputImage = new IIOImage(combined, imgs, null);
		writer.write(null, outputImage, writeParam);
	}
	
	public static void produceMetaInfoFile(JSONObject metaData, String outputDirPath) {
		try {
			FileWriter fw = new FileWriter(outputDirPath);
			fw.write(metaData.toString(1));
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public static class GeneratorSession {
		public final String projectDir;
		public final String blockchain;
		public final int nOfNFTsToGenerate;
		public final List<List<File>> combinations;
		public final ConcurrentHashMap<File, Integer> occurences;
		public final AtomicInteger NFTNameCounter;
		public final String outputImageType;
		public volatile boolean canceled;
		GeneratorSession(
				String projectDir,
				String blockchain,
				int nOfNFTsToGenerate,
				List<List<File>> combinations,
				ConcurrentHashMap<File, Integer> occurences,
				String outputImageType
				){
			this.projectDir = projectDir;
			this.blockchain = blockchain;
			this.nOfNFTsToGenerate = nOfNFTsToGenerate;
			this.combinations = combinations;
			this.occurences = occurences;
			this.outputImageType = outputImageType;
			NFTNameCounter = new AtomicInteger(0);
			canceled = false;
		}
		
		public String createOutputDirectory() {
			String outputDirPath = FileManagement
					.createDirectoryPath(projectDir + '/' + blockchain);
			new File(outputDirPath + "/images").mkdirs();
			new File(outputDirPath + "/metadata").mkdirs();
			return outputDirPath;
		}

		public void createProgressDialog() {
			GeneratorView.getInstance().createProgressDialog(this);
		}
		
	}		
}
