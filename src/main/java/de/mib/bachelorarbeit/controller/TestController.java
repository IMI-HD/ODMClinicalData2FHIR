package de.mib.bachelorarbeit.controller;

import odm.ODM;
import odm.ODMcomplexTypeDefinitionStudy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class TestController {


    @GetMapping("/hello")
    public String sayHello() {
        return "Hello World!";
    }

    @PostMapping(value = "/test", consumes = "application/xml", produces = "application/xml")
    @ResponseBody
    public ResponseEntity<ODM> processXML(@RequestBody ODM odm) {
        List<ODMcomplexTypeDefinitionStudy> study = odm.getStudy();
        System.out.printf("Size of study list: %d", study.size());
        if (!study.isEmpty()) {
            System.out.printf("Name of the study in position 1: %s", study.get(0).getGlobalVariables().getStudyName().getValue());
        }
        return ResponseEntity.status(HttpStatus.OK).body(odm);
    }

}
