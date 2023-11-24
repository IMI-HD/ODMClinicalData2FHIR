package de.mib.bachelorarbeit.services.definitions;

import odm.ODM;

public interface OdmToFhirConverter {

    String getTestRessource();

    void printClinicalData(ODM odm);
}
