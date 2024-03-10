package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class SubjectDataNotFoundException extends ClinicalDataToQuestionnaireResponseException {

    public SubjectDataNotFoundException(String message) {
        super(message);
    }
}
