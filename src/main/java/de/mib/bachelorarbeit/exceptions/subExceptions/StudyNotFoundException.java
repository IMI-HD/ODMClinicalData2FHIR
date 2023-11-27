package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class StudyNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public StudyNotFoundException(String message) {
        super(message);
    }
}
