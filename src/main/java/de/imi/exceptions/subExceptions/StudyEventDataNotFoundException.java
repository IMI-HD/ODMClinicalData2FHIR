package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class StudyEventDataNotFoundException extends ClinicalDataToQuestionnaireResponseException {

    public StudyEventDataNotFoundException(String message) {
        super(message);
    }
}
