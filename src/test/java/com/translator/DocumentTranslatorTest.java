package com.translator;

import org.junit.jupiter.api.Test;

class DocumentTranslatorTest {

	@Test
	void trasnlatetoFRtest() {
		String[] args = {"-filePath", "./src/test/resources/docs/top7-trends-mule-soft.pdf", "-targetLanguage", "fr"} ;
		DocumentTranslator.main(args);
	}
	
	@Test
	void trasnlatetoEStest() {
		String[] args = {"-filePath", "./src/test/resources/docs/top7-trends-mule-soft.pdf", "-targetLanguage", "es"} ;
		DocumentTranslator.main(args);
	}
	
	
	@Test
	void trasnlatetoRUtest() {
		String[] args = {"-fDilePath", "./src/test/resources/docs/top7-trends-mule-soft.pdf", "-targetLanguage", "ru"} ;
		DocumentTranslator.main(args);
	}


}
