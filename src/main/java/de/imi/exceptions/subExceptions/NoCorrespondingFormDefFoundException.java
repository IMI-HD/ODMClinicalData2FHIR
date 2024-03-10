package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoCorrespondingFormDefFoundException extends ClinicalDataToQuestionnaireResponseException {
    public NoCorrespondingFormDefFoundException(String message) {
        super(message);
    }
}
