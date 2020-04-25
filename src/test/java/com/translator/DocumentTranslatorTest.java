package com.translator;

import org.junit.jupiter.api.Test;

class DocumentTranslatorTest {

	@Test
	void translatetoFRtest() {
		String[] args = {"-filePath", "./src/test/resources/docs/top7-trends-mule-soft.pdf", "-targetLanguage", "fr"} ;
		DocumentTranslator.main(args);
	}
	
	@Test
	void translatetoEStest() {
		String[] args = {"-filePath", "./src/test/resources/docs/top7-trends-mule-soft.pdf", "-targetLanguage", "es"} ;
		DocumentTranslator.main(args);
	}
	
	
	@Test
	void translatetoRUtest() {
		String[] args = {"-fDilePath", "./src/test/resources/docs/top7-trends-mule-soft.pdf", "-targetLanguage", "ru"} ;
		DocumentTranslator.main(args);
	}
	
	@Test
	void translateNHSdoctoEStest() {
		String[] args = {"-filePath", "./src/test/resources/docs/ENG-VIR-NHS-GUIDANCE.pdf", "-targetLanguage", "es"} ;
		DocumentTranslator.main(args);
	}


}
