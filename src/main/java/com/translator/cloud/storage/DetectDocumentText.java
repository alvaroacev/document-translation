/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.translator.cloud.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vision.v1.AnnotateFileResponse;
import com.google.cloud.vision.v1.AnnotateFileResponse.Builder;
import com.google.cloud.vision.v1.AsyncAnnotateFileRequest;
import com.google.cloud.vision.v1.AsyncBatchAnnotateFilesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.GcsDestination;
import com.google.cloud.vision.v1.GcsSource;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.InputConfig;
import com.google.cloud.vision.v1.OperationMetadata;
import com.google.cloud.vision.v1.OutputConfig;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.translator.tools.NaturalOrderComparators;
import com.translator.tools.Utils;

public class DetectDocumentText {

	private static final Logger log = LoggerFactory.getLogger(DetectDocumentText.class);

	/**
	 * Performs document text OCR with PDF/TIFF as source files on Google Cloud
	 * Storage.
	 *
	 * @param gcsSourcePath      The path to the remote file on Google Cloud Storage
	 *                           to detect document text on.
	 * @param gcsDestinationPath The path to the remote file on Google Cloud Storage
	 *                           to store the results on.
	 * @return 
	 * @throws Exception on errors while closing the client.
	 */
// --- OCR with PDF can only work if storing the file in Google Cloud Storage https://github.com/GoogleCloudPlatform/cloud-vision/issues/146
	public static String performDocumentOCR(String projectId, String bucketName, String filePath) throws Exception {
		
		final String objectName = Utils.findFileName(filePath);
		final String gcsSourcePath = "gs://" + bucketName + "/" + objectName;
		final String filePrefix = objectName + "-out-";
		final String gcsDestinationPath = "gs://" + bucketName + "/" + filePrefix;

		//First, upload the document to Google Bucket
		uploadObject(projectId, bucketName, objectName, filePath);
		
		try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
			List<AsyncAnnotateFileRequest> requests = new ArrayList<>();

			// Set the GCS source path for the remote file.
			GcsSource gcsSource = GcsSource.newBuilder().setUri(gcsSourcePath).build();

			InputConfig inputConfig = InputConfig.newBuilder().setMimeType("application/pdf") //
					.setGcsSource(gcsSource).build();

			// Set the GCS destination path for where to save the results.
			GcsDestination gcsDestination = GcsDestination.newBuilder().setUri(gcsDestinationPath).build();

			// Configuration for the output with the batch size.
			// The batch size sets how many pages should be grouped into each json output
			// file.
			OutputConfig outputConfig = OutputConfig.newBuilder() //
					.setBatchSize(5) //
					.setGcsDestination(gcsDestination).build();

			Feature feature = Feature.newBuilder().setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();

			// Build the OCR request
			AsyncAnnotateFileRequest request = AsyncAnnotateFileRequest.newBuilder().addFeatures(feature)
					.setInputConfig(inputConfig).setOutputConfig(outputConfig).build();

			requests.add(request);

			// Perform the OCR request
			OperationFuture<AsyncBatchAnnotateFilesResponse, OperationMetadata> response = client
					.asyncBatchAnnotateFilesAsync(requests);

			log.info("Waiting for the operation to finish.");

			// Wait for the request to finish.
			// The result is not used, since the API saves the result to the specified
			// location on GCS.
			response.get(80, TimeUnit.SECONDS).getResponsesList();

			return readFileContent(projectId, bucketName, filePrefix);

		}
	}
	
	private static void uploadObject(String projectId, String bucketName, String objectName, String filePath)
			throws IOException {

		Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
		BlobId blobId = BlobId.of(bucketName, objectName);
		BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
		Blob create = storage.create(blobInfo, Files.readAllBytes(Paths.get(filePath)));

		log.info("File {} uploaded to bucket {} as {}", objectName, create.getBlobId().getBucket(), create.getBlobId().getName());
	}


	private static String readFileContent(String projectId, String bucketName, String filePrefix) throws Exception {
		// Once the request has completed and the output has been
		// written to GCS, we can list all the output files.
		Storage storage = StorageOptions.getDefaultInstance().getService();

		// Get the list of objects with the given prefix from the GCS bucket
		Bucket bucket = storage.get(bucketName);
		com.google.api.gax.paging.Page<Blob> pageList = bucket.list(BlobListOption.prefix(filePrefix));

		List<String> objectNames = new ArrayList<>();
		// List objects with the given prefix.
		for (Blob blob : pageList.iterateAll()) {
			objectNames.add(blob.getName());
		}
		// Sort element following an alpha numerical order
		objectNames.sort(NaturalOrderComparators.createNaturalOrderRegexComparator());
		
		log.debug("List of files to read: {}", objectNames);

		StringBuilder textBuilder = new StringBuilder(); 
    
		objectNames.stream() //
				.forEach(s -> { //
					try {
						textBuilder.append(extractFileContent(projectId, bucketName, s));
						// Remove extracted text objects
						deleteObject(projectId, bucketName, s);
					} catch (InvalidProtocolBufferException e) {
						throw new AssertionError(e);
					}
				});
		
        return textBuilder.toString();
	}

	/**
	 * return the full text of the file passed as parameter, it downloads the
	 * document into a {@code Blob} which then gets converted in to a
	 * {@code AnnotateImageResponse} to easily manipulate the file
	 * 
	 * @param projectId: ID of your GCP project
	 * @param bucketName: ID of your GCS bucket
	 * @param objectName: ID of your GCS object
	 * @return 
	 * @throws InvalidProtocolBufferException
	 */
	private static String extractFileContent(String projectId, String bucketName, String objectName)
			throws InvalidProtocolBufferException {

		Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();

		Blob blob = storage.get(BlobId.of(bucketName, objectName));

		String jsonContents = new String(blob.getContent());
		Builder builder = AnnotateFileResponse.newBuilder();
		JsonFormat.parser().merge(jsonContents, builder);

		AnnotateFileResponse annotateFileResponse = builder.build();

		// Parse through the objects to get the actual response and append the content of each file
		StringBuilder textBuilder = new StringBuilder();
		annotateFileResponse.getResponsesList() //
			.stream() //
			.forEach(s -> textBuilder.append(s.getFullTextAnnotation().getText()));
		
		final String extractedText = textBuilder.toString();
		log.debug("ProjectId: {}, bucket name: {}, object: {}, file content: {}", projectId, bucketName, objectName, extractedText);
		return extractedText;
	}

	private static void deleteObject(String projectId, String bucketName, String objectName) {
		Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();
		storage.delete(bucketName, objectName);

		log.info("Object {} was deleted from {}", objectName, bucketName);
	}
	
}