package com.translator.cloud.translate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.translator.tools.Utils;


class TranslateTextTest {
	

	@Test
	public void translateText() throws IOException {
		 
		 Properties prop = Utils.loadProperties("./src/test/resources/config.properties");
		 String projectId = prop.getProperty("PROJECT_ID");

		 // Supported Languages: https://cloud.google.com/translate/docs/languages
		 String targetLanguage = "fr";
		 String filePath = "./src/test/resources/docs/extractedText.txt";
		 String textToTranslate = new String(Files.readAllBytes(Paths.get(filePath)));
		 
		 System.out.println(TranslateText.translateText(projectId, targetLanguage, textToTranslate));
		}


}
