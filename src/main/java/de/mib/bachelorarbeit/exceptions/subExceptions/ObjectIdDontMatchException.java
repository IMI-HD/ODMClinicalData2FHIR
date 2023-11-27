package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ObjectIdDontMatchException extends ClinicalDataToQuestionnaireResponseException {
    public ObjectIdDontMatchException(String message) {
        super(message);
    }
}
