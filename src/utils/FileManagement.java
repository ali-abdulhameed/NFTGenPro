package utils;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class FileManagement {
	
	public static final String[] IMG_INPUT_TYPES;
	public static final String[] IMG_OUTPUT_TYPES;
	static {

		IMG_INPUT_TYPES = 
				Arrays.stream(ImageTypes.Input.values())
				.map(Enum::name)
				.toArray(String[]::new);
		IMG_OUTPUT_TYPES =
				Arrays.stream(ImageTypes.Output.values())
				.map(Enum::name)
				.toArray(String[]::new);
	
	}
	private FileManagement() {}
	
	public static File[] listAllFiles(String baseFolderPath, String filter) {
		return new File(baseFolderPath).listFiles(new FileFilter() {
		    @Override
		    public boolean accept(File file) {
		    	if(filter == null) return true;
		        switch(filter.toLowerCase()) {
		        case "dir": return file.isDirectory();
		        case "img" : return isImage(file.getName());
		        case "json": return file.getName().toLowerCase().endsWith(".json");
		        default: return false;
		        }
		    }
		});
	}
	
	public static String createDirectoryPath(String path) {
		int i = 1;
		String newPath = path;
		while(new File(newPath).exists()) {
			newPath = path + "-" + i++;
		}
		return newPath;
	}
	
	public static boolean isImage(String file) {
		String f = file.toUpperCase();
		return Arrays.stream(IMG_INPUT_TYPES).anyMatch(f::endsWith);
	}
	
	public static boolean isDigit(String value) {
		try {
			Integer.parseInt(value);
			return true;
		} catch(Exception e) {
			return false;
		}
	}
	
}
