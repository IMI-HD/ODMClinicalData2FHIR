package de.mib.bachelorarbeit.controller;

import de.mib.bachelorarbeit.responses.ParseOdmResponse;
import odm_v1_3.ODM;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {


    @GetMapping("/hello")
    public String sayHello() {
        return "Hello World!";
    }

    @PostMapping(value = "/test", consumes = "application/xml", produces = "application/xml")
    @ResponseBody
    public ResponseEntity<ODM> processXML(@RequestBody ODM odm) {
        ODM.Study study = odm.getStudy();
        System.out.printf("Name of the received Study: %s \n", study.getGlobalVariables().getStudyName());
        return ResponseEntity.status(HttpStatus.OK).body(odm);
    }

}
