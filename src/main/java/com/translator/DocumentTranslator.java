package com.translator;

import java.nio.file.NoSuchFileException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.translator.cloud.storage.DetectDocumentText;
import com.translator.cloud.translate.TranslateText;
import com.translator.tools.Utils;

public class DocumentTranslator {
	private static final int API_REQUEST_SIZE = 2000;
	private static final Logger log = LoggerFactory.getLogger(DocumentTranslator.class);

	public static void main(String[] args) {
		final Options options = new Options();
		options.addOption(Option.builder("filePath").argName("filePath").hasArg().desc("Path to the file to translate.").required().build());
		options.addOption(Option.builder("targetLanguage").argName("targetLanguage").hasArg().desc("Target language. Please use ISO-639-1 code identifiers, for example fr for French").required().build());
		options.addOption(Option.builder("configPath").argName("configPath").hasArg().desc("Path to the configuration file describing Google Cloud project Id and Bucket Name").required(false).build());
		
		CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

		try {
			cmd = parser.parse(options, args);

			String filePath = cmd.getOptionValue("filePath");
			String targetLanguage = cmd.getOptionValue("targetLanguage");
			String configPath = cmd.getOptionValue("configPath");
			
			if(configPath == null || configPath.isEmpty()) {
				configPath = "./src/test/resources/config.properties";
			}
			Properties prop = Utils.loadProperties(configPath);

			String projectId = prop.getProperty("PROJECT_ID");
			String text = DetectDocumentText.performDocumentOCR(projectId, prop.getProperty("BUCKET_NAME"), filePath);
			log.debug("Extracted file information: \n {}", text);
			log.info("Number of characters in extracted text: {}", text.length());
			
			// The recommended maximum length for each request is 2K
			StringBuilder textBuilder = new StringBuilder();
			for (int i = 0; i < text.length(); i += API_REQUEST_SIZE) {
				textBuilder.append(TranslateText.translateText(projectId, //
						targetLanguage, //
						text.substring(i, Math.min(text.length(), i + API_REQUEST_SIZE)) //
				));
			}
			text = textBuilder.toString();
			log.info("Translated text: \n {}", text);
		
		} catch (ParseException e) {
			log.error("Parsing failed.  Reason: {}", e.getMessage());
			formatter.printHelp("Document translator: {}", options);
			System.exit(1);
		} catch (NoSuchFileException e) {
			log.error("Cannot load resource {}, {}", e.getMessage(), e);
			System.exit(1);
		} catch (Exception e) {
			log.error("Error occured: {}, {}", e.getMessage(), e);
			System.exit(1);
		}		

	}
	

}
