package de.mib.bachelorarbeit.controller;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;
import de.mib.bachelorarbeit.services.definitions.OdmToFhirConverter;
import odm.ODM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConverterController {


    private final static Logger LOGGER = LoggerFactory.getLogger(ConverterController.class);

    private final OdmToFhirConverter odmToFhirConverter;

    @Autowired
    public  ConverterController(
            OdmToFhirConverter odmToFhirConverter
    ) {
        this.odmToFhirConverter = odmToFhirConverter;
    }

    @PostMapping(value = "/converter", consumes = "application/xml")
    @ResponseBody
    public ResponseEntity<String> convertClinicalDataToQuestionnaireResponse(@RequestBody ODM odm) {
        try {
            odmToFhirConverter.clinicalDataToQuestionnaireResponse(odm);
            return ResponseEntity.status(HttpStatus.OK).body("Great success!");
        } catch (ClinicalDataToQuestionnaireResponseException e) {
            throw new RuntimeException(e);
        }
    }

}
