package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ObjectIdDontMatchException extends ClinicalDataToQuestionnaireResponseException {
    public ObjectIdDontMatchException(String message) {
        super(message);
    }
}
