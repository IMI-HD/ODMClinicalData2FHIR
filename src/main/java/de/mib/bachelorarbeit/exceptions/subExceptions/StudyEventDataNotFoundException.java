package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class StudyEventDataNotFoundException extends ClinicalDataToQuestionnaireResponseException {

    public StudyEventDataNotFoundException(String message) {
        super(message);
    }
}
