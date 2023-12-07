package de.mib.bachelorarbeit.controller;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;
import de.mib.bachelorarbeit.responses.ConverterErrorResponse;
import de.mib.bachelorarbeit.services.definitions.OdmToFhirConverter;
import odm.ODM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class ConverterController {


    private final static Logger LOGGER = LoggerFactory.getLogger(ConverterController.class);

    private final OdmToFhirConverter odmToFhirConverter;

    @Autowired
    public ConverterController(
            OdmToFhirConverter odmToFhirConverter
    ) {
        this.odmToFhirConverter = odmToFhirConverter;
    }

    @PostMapping(value = "/converter", consumes = "application/xml", produces = "application/json")
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

    private boolean checkIfLanguageIsKnown(String language) {
        if (language.equals("english")) {
            LOGGER.info("Detected language: 'english'");
            return true;
        }
        if (language.equals("german")) {
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
