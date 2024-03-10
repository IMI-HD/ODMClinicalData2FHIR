package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class MetaDataVersionNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public MetaDataVersionNotFoundException(String message) {
        super(message);
    }
}
