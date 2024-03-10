package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoCorrespondingStudyEventDefFoundException extends ClinicalDataToQuestionnaireResponseException {
    public NoCorrespondingStudyEventDefFoundException(String message) {
        super(message);
    }
}
