package de.imi.services.definitions;

import de.imi.exceptions.UnmarshallingOdmException;
import odm.ODM;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UnmarshallingService {

    ODM unmarshallOdmFromMultipartFile(MultipartFile odmFile) throws UnmarshallingOdmException;
}
