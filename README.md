# Document Translation


This translates long documents text to a target language by leveraging Google Translation APIs. The translation process is composed of two major tasks:

1. Detect text from an input document 
2. Translate text

Text detection utilizes the Google Vision APIs which is pre-trained to perform a very accurate  optical character recognition (OCR). Because Vision APIs limits the size of the API request to 10 MB, the input file is then uploaded to a Cloud Storage Bucket and from there the text detection is performed.

The software builds a string with the result of the OCR operation and sends it for Translation and the result is written to the log file or console.


## Getting Started

You can run this software as a CLI, or using the unit tests.

### Prerequisites

Two simple steps before playing with this project

#### Create a service account

To run this project using CLI or running the Unit Test set an environment variable `GOOGLE_APPLICATION_CREDENTIALS` which points to your service_account identity setup with Google. See here [Getting started guide](https://cloud.google.com/docs/authentication/getting-started)


#### Config file
Create a configuration file describing two global variables: 
1. PROJECT_ID: name of the project in Google Cloud
2. BUCKET_NAME the bucket you pre-configured to store documents and detected text, an example of configuration file can be found in this project see: 
/src/test/resources/config.properties and for more information about the creating a Google Cloud project and bucket see these two guides:

* [Creating and Managing Projects](https://cloud.google.com/resource-manager/docs/creating-managing-projects)
* [Creating storage buckets](https://cloud.google.com/storage/docs/creating-buckets)


```
usage: Document translator: {}
 -configPath <configPath>           Path to the configuration file describing Google Cloud project Id and Bucket Name
 -filePath <filePath>               Path to the file to translate.
 -targetLanguage <targetLanguage>   Target language. Please use ISO-639-1 code identifiers, for example 'fr' for French

```

## Built With

* [Maven](https://maven.apache.org/) - Dependency Management

## Contributing

Please feel free to contribute to this project and submit your push requests.

## Version

Next version of this project will utilize a local library to perform the OCR step