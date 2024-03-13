package de.imi.controller;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;
import de.imi.exceptions.UnmarshalOdmException;
import de.imi.responses.ConverterErrorResponse;
import de.imi.services.definitions.OdmToFhirConverter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import odm.ODM;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.PDNameTreeNode;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.hl7.fhir.r4.model.Bundle;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@Tag(name = "Converter Controller")
public class ConverterController {


    private final static Logger LOGGER = LoggerFactory.getLogger(ConverterController.class);

    private final OdmToFhirConverter odmToFhirConverter;

    private Unmarshaller odmUnmarshaller;

    @Autowired
    public ConverterController(
            OdmToFhirConverter odmToFhirConverter
    ) throws JAXBException {
        this.odmToFhirConverter = odmToFhirConverter;
        JAXBContext jaxbContextOdm = JAXBContext.newInstance(ODM.class);
        this.odmUnmarshaller = jaxbContextOdm.createUnmarshaller();
    }

    @Operation(
            description = "POST endpoint of the conversion service",
            summary = "Takes an ODM file (XML format) and converts it to a FHIR Bundle (JSON).",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successful conversion of ODM file to FHIR Bundle",
                            content = @Content(
                                    mediaType = "application/json;charset=UTF-8",
                                    schema = @Schema(implementation = Bundle.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Bad Request - Error in conversion process",
                            content = @Content(
                                    mediaType = "application/json;charset=UTF-8",
                                    schema = @Schema(implementation = ConverterErrorResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error",
                            content = @Content(
                                    mediaType = "application/json;charset=UTF-8",
                                    schema = @Schema(implementation = ConverterErrorResponse.class)
                            )
                    )
            },
            parameters = {
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "questionnaire-language",
                            description = "Language Code the converter resolves in the given ODM",
                            required = true,
                            schema = @Schema(type = "string")
                    ),
                    @Parameter(
                            in = ParameterIn.HEADER,
                            name = "questionnaire-link",
                            description = "URL to the Structure Definition of the given Questionnaire",
                            required = true,
                            schema = @Schema(type = "string")
                    )
            }
    )
    @PostMapping(value = "/converter", consumes = "application/xml;charset=UTF-8",
            produces = "application/json;charset=UTF-8")
    @ResponseBody
    public ResponseEntity<Object> convertClinicalDataToQuestionnaireResponse(
            @RequestBody ODM odm,
            @RequestHeader Map<String, String> headers
    ) {
        LOGGER.info("/converter endpoint hit!");
        LOGGER.info("checking for headers");
        // check if required headers are present
        if (!headers.containsKey("questionnaire-language")) {
            String error = "Header did not contain key: 'questionnaire-language'";
            LOGGER.error(error);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ConverterErrorResponse(
                            LocalDateTime.now().toString(),
                            HttpStatus.BAD_REQUEST.value(),
                            error,
                            "/converter"
                    ));
        }
        if (!headers.containsKey("questionnaire-link")) {
            String error = "Header did not contain key: 'questionnaire-link'";
            LOGGER.error(error);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ConverterErrorResponse(
                            LocalDateTime.now().toString(),
                            HttpStatus.BAD_REQUEST.value(),
                            error,
                            "/converter"
                    ));
        }
        // data fields for header values
        String language = headers.get("questionnaire-language");
        String link = headers.get("questionnaire-link");
        // check value of headers fields
        if (language.isEmpty() || language.isBlank()) {
            String error = "Value of key: 'questionnaire-language' was empty or blank";
            LOGGER.error(error);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ConverterErrorResponse(
                            LocalDateTime.now().toString(),
                            HttpStatus.BAD_REQUEST.value(),
                            error,
                            "/converter"
                    ));
        }
        if (!checkIfLanguageIsKnown(language)) {
            String error = "Value of key: 'questionnaire-language' is not known!";
            LOGGER.error(error);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ConverterErrorResponse(
                            LocalDateTime.now().toString(),
                            HttpStatus.BAD_REQUEST.value(),
                            error,
                            "/converter"
                    ));
        }
        if (link.isEmpty() || link.isBlank()) {
            String error = "Value of key: 'questionnaire-link' was empty or blank";
            LOGGER.error(error);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ConverterErrorResponse(
                            LocalDateTime.now().toString(),
                            HttpStatus.BAD_REQUEST.value(),
                            error,
                            "/converter"
                    ));
        }
        // call conversion method
        try {
            LOGGER.info("/converter endpoint hit!");
            String bundle = odmToFhirConverter.clinicalDataToQuestionnaireResponse(odm, language, link);
            return ResponseEntity.status(HttpStatus.OK).body(bundle);
        } catch (ClinicalDataToQuestionnaireResponseException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ConverterErrorResponse(
                            LocalDateTime.now().toString(),
                            HttpStatus.BAD_REQUEST.value(),
                            e.getMessage(),
                            "/converter"
                    ));
        }
    }

    @PostMapping("/attach/pdf")
    public ResponseEntity<byte[]> attachJsonToPdf(
            @RequestParam("pdf") MultipartFile pdfFile,
            @RequestParam("json") MultipartFile json
    ) {
        LOGGER.info("/attach/pdf endpoint hit!");
        try {
            Path tempPdfPath = Files.createTempFile("uploaded", ".pdf");
            File tempPdfFile = tempPdfPath.toFile();
            pdfFile.transferTo(tempPdfFile);
            byte[] jsonBytes = json.getBytes();

            try (PDDocument document = Loader.loadPDF(tempPdfFile);
                 ByteArrayInputStream jsonInputStream = new ByteArrayInputStream(jsonBytes);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                PDEmbeddedFilesNameTreeNode efTree = getPdEmbeddedFilesNameTreeNode(document, jsonInputStream);

                // Hinzufügen des Baums der eingebetteten Dateien zum Dokument
                PDDocumentCatalog catalog = document.getDocumentCatalog();
                if (catalog.getNames() == null) {
                    catalog.setNames(new PDDocumentNameDictionary(catalog));
                }
                catalog.getNames().setEmbeddedFiles(efTree);

                document.save(out);

                return ResponseEntity.ok()
                        .header("Content-Type", "application/pdf")
                        .body(out.toByteArray());
            }

        } catch (IOException e) {
            LOGGER.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not Okay!".getBytes());
        }

    }

    @PostMapping("/converter/pdf")
    public ResponseEntity<byte[]> attachFhirOntoPdf(
            @RequestParam("pdf") MultipartFile pdfFile,
            @RequestParam("odm") MultipartFile odm,
            @RequestHeader Map<String, String> headers
    ) {
        LOGGER.info("/converter/pdf endpoint hit!");
        try (InputStream odmInputStream = odm.getInputStream()) {

            Path tempPdfPath = Files.createTempFile("uploaded", ".pdf");
            File tempPdfFile = tempPdfPath.toFile();

            pdfFile.transferTo(tempPdfFile);

            ODM odmObject = (ODM) odmUnmarshaller.unmarshal(odmInputStream);

            //Todo: insert checks from above here!
            String language = headers.get("questionnaire-language");
            String link = headers.get("questionnaire-link");

            String bundle = odmToFhirConverter.clinicalDataToQuestionnaireResponse(odmObject, language, link);

            try (PDDocument document = Loader.loadPDF(tempPdfFile);
                 ByteArrayInputStream jsonByteInputStream = new ByteArrayInputStream(bundle.getBytes());
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                // Erstellen eines neuen Embedded File
                PDEmbeddedFilesNameTreeNode efTree = getPdEmbeddedFilesNameTreeNode(document, jsonByteInputStream);

                // Hinzufügen des Baums der eingebetteten Dateien zum Dokument
                PDDocumentCatalog catalog = document.getDocumentCatalog();
                if (catalog.getNames() == null) {
                    catalog.setNames(new PDDocumentNameDictionary(catalog));
                }
                catalog.getNames().setEmbeddedFiles(efTree);

                document.save(out);

                return ResponseEntity.ok()
                        .header("Content-Type", "application/pdf")
                        .body(out.toByteArray());

            } catch (IOException e) {
                LOGGER.error(e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not Okay!".getBytes());
            }


        } catch (IOException | JAXBException | ClinicalDataToQuestionnaireResponseException e) {
            StackTraceElement[] stackTraceElement = e.getStackTrace();
            StringBuilder builder = new StringBuilder();
            for (StackTraceElement s : stackTraceElement) {
                builder.append(s.toString()).append("\n");
            }
            LOGGER.error(builder.toString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Not Okay!".getBytes());
        }
    }

    @NotNull
    private static PDEmbeddedFilesNameTreeNode getPdEmbeddedFilesNameTreeNode
            (PDDocument document, ByteArrayInputStream jsonByteInputStream) throws IOException {
        PDEmbeddedFile embeddedFile = new PDEmbeddedFile(document, jsonByteInputStream);
        embeddedFile.setSubtype("application/json"); // Setzen des MIME-Typs

        PDComplexFileSpecification fs = new PDComplexFileSpecification();
        fs.setEmbeddedFile(embeddedFile);
        fs.setFile("fhirBundle.json"); // Name des Anhangs im PDF

        PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();
        HashMap<String, PDComplexFileSpecification> names = new HashMap<>();
        names.put("fhirBundle.json", fs); // Fügt den Anhang mit dem Namen hinzu
        efTree.setNames(names);
        return efTree;
    }

    private boolean checkIfLanguageIsKnown(String language) {
        if (language.equals("en")) {
            LOGGER.info("Detected language: 'english'");
            return true;
        }
        if (language.equals("de")) {
            LOGGER.info("Detected language: 'german'");
            return true;
        }
        LOGGER.warn(String.format(
                "Given language: '%s' is not known!",
                language
        ));
        return false;
    }

}
