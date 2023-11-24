package de.mib.bachelorarbeit.controller;

import de.mib.bachelorarbeit.services.definitions.OdmToFhirConverter;
import odm.ODM;
import odm.ODMcomplexTypeDefinitionStudy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TestController {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestController.class);

    private final OdmToFhirConverter odmToFhirConverter;

    @Autowired
    public TestController(
            OdmToFhirConverter odmToFhirConverter
    ) {
        this.odmToFhirConverter = odmToFhirConverter;
    }

    @GetMapping("/hello")
    public String sayHello() {
        return "Hello World!";
    }

    @PostMapping(value = "/test", consumes = "application/xml", produces = "application/xml")
    @ResponseBody
    public ResponseEntity<ODM> processXML(@RequestBody ODM odm) {
        LOGGER.info("/test Endpoint hit!");
        List<ODMcomplexTypeDefinitionStudy> study = odm.getStudy();
        System.out.printf("Size of study list: %d", study.size());
        if (!study.isEmpty()) {
            System.out.printf("Name of the study in position 1: %s", study.get(0).getGlobalVariables().getStudyName().getValue());
        }
        return ResponseEntity.status(HttpStatus.OK).body(odm);
    }

    @GetMapping(value = "/patient", produces = "application/json")
    public ResponseEntity<String> getTestPatient() {
        String serializedPatient = odmToFhirConverter.getTestRessource();
        return ResponseEntity
                .status(HttpStatus.OK)
                .header("Content-Type", "application/fhir+json")
                .body(serializedPatient);
    }

    @PostMapping(value = "/print", consumes = "application/xml")
    public ResponseEntity<String> printODMClinicalData(@RequestBody ODM odm) {
        LOGGER.info("/print Endpoint hit!");
        odmToFhirConverter.printClinicalData(odm);
        return ResponseEntity.status(HttpStatus.OK).body("See Logs for info!");
    }


}
