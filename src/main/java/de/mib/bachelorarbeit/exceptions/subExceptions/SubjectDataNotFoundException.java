package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class SubjectDataNotFoundException extends ClinicalDataToQuestionnaireResponseException {

    public SubjectDataNotFoundException(String message) {
        super(message);
    }
}
