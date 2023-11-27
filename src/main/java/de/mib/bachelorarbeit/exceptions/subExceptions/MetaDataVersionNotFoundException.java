package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class MetaDataVersionNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public MetaDataVersionNotFoundException(String message) {
        super(message);
    }
}
