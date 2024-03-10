package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class StudyEventDescriptionNotFound extends ClinicalDataToQuestionnaireResponseException {
    public StudyEventDescriptionNotFound(String message) {
        super(message);
    }
}
