package com.translator.cloud.translate;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.cloud.translate.v3.LocationName;
import com.google.cloud.translate.v3.TranslateTextRequest;
import com.google.cloud.translate.v3.TranslateTextResponse;
import com.google.cloud.translate.v3.TranslationServiceClient;

public class TranslateText {

	private static final Logger log = LoggerFactory.getLogger(TranslateText.class);

	public static String translateText(String projectId, String targetLanguage, String text) throws IOException {

		String translatedText = "";
		try (TranslationServiceClient client = TranslationServiceClient.create()) {
			LocationName parent = LocationName.of(projectId, "global");

			TranslateTextRequest request = TranslateTextRequest.newBuilder() //
					.setParent(parent.toString()) //
					.setMimeType("text/plain") //
					.setTargetLanguageCode(targetLanguage) //
					.addContents(text).build();

			TranslateTextResponse response = client.translateText(request);

			StringBuilder textBuilder = new StringBuilder();
			response.getTranslationsList().stream() //
					.forEach(s -> {
						textBuilder.append(s.getTranslatedText());
					});
			
			translatedText = textBuilder.toString();
			log.debug("Translater text: {}", translatedText);
		}
		return translatedText;
	}
}