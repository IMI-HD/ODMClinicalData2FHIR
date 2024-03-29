package de.imi.controller;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;
import de.imi.exceptions.UnmarshallingOdmException;
import de.imi.responses.ConverterErrorResponse;
import de.imi.services.definitions.OdmToFhirConverter;
import de.imi.services.definitions.UnmarshallingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import odm.ODM;
import org.hl7.fhir.r4.model.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@Tag(name = "Converter Controller")
public class ConverterController {


    private final static Logger LOGGER = LoggerFactory.getLogger(ConverterController.class);

    private final OdmToFhirConverter odmToFhirConverter;

    private final UnmarshallingService unmarshallingService;

    @Autowired
    public ConverterController(
            OdmToFhirConverter odmToFhirConverter,
            UnmarshallingService unmarshallingService
    ) {
        this.odmToFhirConverter = odmToFhirConverter;
        this.unmarshallingService = unmarshallingService;
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
    @PostMapping(
            value = "/converter",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseBody
    public ResponseEntity<Object> convertClinicalDataToQuestionnaireResponse(
            @RequestParam("odm") MultipartFile odmFile,
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
        try {
            LOGGER.info("Unmarshalling received ODM file");
            // unmarshall multipart file with service
            ODM odmObject = unmarshallingService.unmarshallOdmFromMultipartFile(odmFile);
            // send object to converter
            LOGGER.info("Converting received ODM file");
            String bundle = odmToFhirConverter.clinicalDataToQuestionnaireResponse(odmObject, language, link);
            return ResponseEntity.status(HttpStatus.OK).body(bundle);
        } catch (ClinicalDataToQuestionnaireResponseException | UnmarshallingOdmException e) {
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
