package de.imi.services.implementations;

import de.imi.services.definitions.LoadOdmFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class LoadOdmFileServiceImpl implements LoadOdmFileService {


    private final static Logger LOGGER = LoggerFactory.getLogger(LoadOdmFileService.class);

    @Override
    public byte[] loadFileWithoutId() {
        try {
            ClassPathResource xmlFile = new ClassPathResource("testFiles/WHO5 repeating/WHO5WithTestDataRepeating.xml");
            return xmlFile.getContentAsByteArray();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            LOGGER.warn("Returning empty byte[] array");
            return new byte[0];
        }
    }
}
