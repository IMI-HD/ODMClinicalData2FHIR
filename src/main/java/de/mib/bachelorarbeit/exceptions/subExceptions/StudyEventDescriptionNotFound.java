package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class StudyEventDescriptionNotFound extends ClinicalDataToQuestionnaireResponseException {
    public StudyEventDescriptionNotFound(String message) {
        super(message);
    }
}
