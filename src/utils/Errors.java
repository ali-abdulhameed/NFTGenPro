package utils;

public enum Errors {
	DirectoryNotSelected,
	InvalidDirectory,
	BlockchainNotSelected,
	OutputImageTypeNotSelected,
	NoNFTLayersWereFound,
	GenericError,
	CustomError,
	UpdateMetaFieldsNotSet,
	NoErrorDetected;
	private static String generatorCustomError = "";
	private static String updaterCustomError = "";
	public static void setGeneratorCustomError(String error) {
		generatorCustomError = error;
	}
	public static void setUpdaterCustomError(String error) {
		updaterCustomError = error;
	}
	public static String getGeneratorCustomError() {
		String error = generatorCustomError;
		generatorCustomError = "";
		return error;
	}
	public static String getUpdaterCustomError() {
		String error = updaterCustomError;
		updaterCustomError = "";
		return error;
	}
}
