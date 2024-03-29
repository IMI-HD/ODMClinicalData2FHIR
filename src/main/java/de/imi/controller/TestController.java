package de.imi.controller;

import de.imi.services.definitions.OdmToFhirConverter;
import io.swagger.v3.oas.annotations.Hidden;
import odm.ODM;
import odm.ODMcomplexTypeDefinitionStudy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@Hidden
public class TestController {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestController.class);

    private final OdmToFhirConverter odmToFhirConverter;

    private final Environment env;

    @Autowired
    public TestController(
            OdmToFhirConverter odmToFhirConverter,
            Environment env
    ) {
        this.odmToFhirConverter = odmToFhirConverter;
        this.env = env;
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

    @Deprecated
    @GetMapping(value = "/patient", produces = "application/json")
    public ResponseEntity<String> getTestPatient() {
        String serializedPatient = "{empty}";
        return ResponseEntity
                .status(HttpStatus.OK)
                .header("Content-Type", "application/fhir+json")
                .body(serializedPatient);
    }

    @Deprecated
    @PostMapping(value = "/print", consumes = "application/xml")
    public ResponseEntity<String> printODMClinicalData(@RequestBody ODM odm) {
        LOGGER.info("/print Endpoint hit!");
        LOGGER.warn("Method print is not implemented anymore!");
        return ResponseEntity.status(HttpStatus.OK).body("See Logs for info!");
    }

    @GetMapping(value = "/env")
    public String getEnvVariable() {
        return env.getProperty("coding");
    }

    @PostMapping(value = "/header")
    public void printHeaders(
            @RequestHeader Map<String, String> headers
    ) {
        LOGGER.info("Received headers:\n");
        headers.forEach((key, value) -> LOGGER.info(
                String.format(
                        "key: '%s' and value: '%s'",
                        key,
                        value
                )
        ));
    }

    @GetMapping(value = "/testString", produces = "application/json")
    public String test() {
        return """
                { "message": "Hello world!" }
                """;
    }


}
