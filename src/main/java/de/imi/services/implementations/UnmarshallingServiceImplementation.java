package de.imi.services.implementations;

import de.imi.exceptions.UnmarshallingOdmException;
import de.imi.services.definitions.UnmarshallingService;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import odm.ODM;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class UnmarshallingServiceImplementation implements UnmarshallingService {


    private final static Logger LOGGER = LoggerFactory.getLogger(UnmarshallingServiceImplementation.class);

    private final Unmarshaller unmarshaller;

    public UnmarshallingServiceImplementation() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(ODM.class);
        this.unmarshaller = jaxbContext.createUnmarshaller();
    }

    @Override
    public ODM unmarshallOdmFromMultipartFile(MultipartFile odmFile) throws UnmarshallingOdmException {
        try (InputStream odmInputStream = odmFile.getInputStream()) {
            return (ODM) unmarshaller.unmarshal(odmInputStream);
        } catch (IOException | JAXBException e) {
            LOGGER.error(e.getMessage());
            throw new UnmarshallingOdmException(e.getMessage());
        }
    }
}
