package de.imi.controller;

import de.imi.services.definitions.LoadOdmFileService;
import odm.ODM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OdmFileController {

    private final LoadOdmFileService loadOdmFileService;

    private final static Logger LOGGER = LoggerFactory.getLogger(OdmFileController.class);

    @Autowired
    public OdmFileController(
            LoadOdmFileService loadOdmFileService
    ) {
        this.loadOdmFileService = loadOdmFileService;
    }


    @GetMapping(value = "/odm", produces = MediaType.APPLICATION_ATOM_XML_VALUE)
    public ResponseEntity<byte[]> getFile() {
        LOGGER.info("/odm endpoint hit!");
        return ResponseEntity.ok().body(loadOdmFileService.loadFileWithoutId());
    }

}
