package com.translator;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;

class DocumentTranslatorTest {

	@Ignore
	void trasnlatetoFRtest() {
		String[] args = {"-filePath", "./src/test/resources/docs/asylum.gov.uk.pdf", "-targetLanguage", "fr"} ;
		DocumentTranslator.main(args);
	}
	
	@Test
	void trasnlatetoEStest() {
		String[] args = {"-filePath", "./src/test/resources/docs/asylum.gov.uk.pdf", "-targetLanguage", "es"} ;
		DocumentTranslator.main(args);
	}


}
