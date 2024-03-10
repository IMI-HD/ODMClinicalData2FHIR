package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class StudyNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public StudyNotFoundException(String message) {
        super(message);
    }
}
