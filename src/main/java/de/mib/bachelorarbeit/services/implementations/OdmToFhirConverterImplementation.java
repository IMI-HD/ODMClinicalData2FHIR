package de.mib.bachelorarbeit.services.implementations;


import ca.uhn.fhir.context.FhirContext;
import de.mib.bachelorarbeit.services.definitions.OdmToFhirConverter;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

@Service
public class OdmToFhirConverterImplementation implements OdmToFhirConverter {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OdmToFhirConverterImplementation.class);

    private final FhirContext fhirContext = FhirContext.forR4();

    @Override
    public String getTestRessource() {
        Patient patient = new Patient();
        patient.addIdentifier()
                .setSystem("http://example.com/fhir/patient-identifiers")
                .setValue("12345");
        patient.addName()
                .setFamily("Mustermann")
                .addGiven("Max");
        LOGGER.info("Test patient created!");

        return fhirContext.newJsonParser().setPrettyPrint(true).encodeToString(patient);
    }
}
