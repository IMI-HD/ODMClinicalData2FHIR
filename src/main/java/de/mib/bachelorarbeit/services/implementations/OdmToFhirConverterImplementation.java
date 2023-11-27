package de.mib.bachelorarbeit.services.implementations;


import ca.uhn.fhir.context.FhirContext;
import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;
import de.mib.bachelorarbeit.exceptions.subExceptions.*;
import de.mib.bachelorarbeit.services.definitions.OdmToFhirConverter;
import odm.*;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.QuestionnaireResponse;
import org.hl7.fhir.r4.model.Reference;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class OdmToFhirConverterImplementation implements OdmToFhirConverter {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OdmToFhirConverterImplementation.class);

    private final FhirContext fhirContext = FhirContext.forR4();

    @Override
    public String getTestRessource() {
        Patient patient = new Patient();
        patient.addIdentifier()
                .setSystem("http://example.com/fhir/patient-identifiers")
                .setValue("12345");
        patient.addName()
                .setFamily("Mustermann")
                .addGiven("Max");
        LOGGER.info("Test patient created!");

        return fhirContext.newJsonParser().setPrettyPrint(true).encodeToString(patient);
    }

    @Override
    public String clinicalDataToQuestionnaireResponse(@NotNull ODM odm)
            throws ClinicalDataToQuestionnaireResponseException {
        LOGGER.info("Received ODM in the converter!");
        // FHIR Bundle that will be returned
        Bundle returnBundle = createBundle();
        // List of all QRS contained in the final Bundle
        List<QuestionnaireResponse> _qrs = new ArrayList<>();
        // List of <ClinicalData> Elements from ODM
        List<ODMcomplexTypeDefinitionClinicalData> _clinicalData;
        // List of <Study> Elements from ODM
        List<ODMcomplexTypeDefinitionStudy> _study;
        // <ClinicalData> Element at index 0 (only this will be converted!)
        ODMcomplexTypeDefinitionClinicalData clinicalData;
        // <SubjectData> Element at index 0 (only this will be converted!)
        ODMcomplexTypeDefinitionSubjectData subjectData;
        // <Study> Element at index 0 (only this will be converted!)
        ODMcomplexTypeDefinitionStudy study;
        // <MetaDataVersion> Element at index 0 of the <Study> (only this will be converted!)
        ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion;

        // validate basic structure of the ODM file

        // check if a <ClinicalData> Element is present!
        if (odm.getClinicalData().isEmpty()) {
            LOGGER.error("<ClinicalData> was empty!");
            throw new ClinicalDataNotFoundException("<ClinicalData> was empty!");
        } else {
            // set <ClinicalData> from ODM
            LOGGER.info("Set <ClinicalData> list!");
            _clinicalData = odm.getClinicalData();
            try {
                clinicalData = _clinicalData.get(0);
                LOGGER.info("<ClinicalData> field set!");
            } catch (IndexOutOfBoundsException e) {
                LOGGER.error("No <ClinicalData> found at index 0!");
                throw new ClinicalDataNotFoundException("No <ClinicalData> found at index 0!");
            }
        }

        // check if a <SubjectData> Element is present in the selected <ClinicalData>
        if (clinicalData.getSubjectData().isEmpty()) {
            LOGGER.error("<SubjectData> was empty!");
            throw new SubjectDataNotFoundException("<SubjectData> was empty!");
        } else {
            // set <SubjectData> at index 0 from selected <ClinicalData>
            try {
                subjectData = clinicalData.getSubjectData().get(0);
                LOGGER.info("<SubjetData> field set!");
            } catch (IndexOutOfBoundsException e) {
                LOGGER.error("No <SubjectData> found at index 0!");
                throw new SubjectDataNotFoundException("No <SubjectData> found at index 0!");
            }
        }

        // check if a <Study> Element is present!
        if (odm.getStudy().isEmpty()) {
            LOGGER.error("<Study> was empty!");
            throw new StudyNotFoundException("<Study> was empty!");
        } else {
            // set <Study> from ODM
            LOGGER.info("Set <Study> list!");
            _study = odm.getStudy();
            try {
                study = _study.get(0);
                LOGGER.info("<Study> field set!");
            } catch (IndexOutOfBoundsException e) {
                LOGGER.error("No <Study> found at index 0!");
                throw new StudyNotFoundException("No <Study> found at index 0!");
            }
        }

        // check if a <MetaDataVersion> from the selected <Study> is present!
        if (study.getMetaDataVersion().isEmpty()) {
            LOGGER.error("<MetaDataVersion> was empty!");
            throw new MetaDataVersionNotFoundException("<MetaDataVersion> was empty!");
        } else {
            // set <MetaDataVersion> at index 0 from selected <Study> from ODM
            try {
                metaDataVersion = study.getMetaDataVersion().get(0);
                LOGGER.info("<MetaDataVersion> field set!");
            } catch (IndexOutOfBoundsException e) {
                LOGGER.error("No <MetaDataVersion> found at index 0!");
                throw new MetaDataVersionNotFoundException("No <MetaDataVersion> found at index 0!");
            }
        }

        LOGGER.info("All data fields set! Looping through ODM");

        // check if <Study> and <ClinicalData> OIDs match
        if (!clinicalData.getStudyOID().equals(study.getOID())) {
            String errorString = String.format("<ClinicalData> StudyOID: %s does not match provided <Study> OID: %s",
                    clinicalData.getStudyOID(), study.getOID());
            LOGGER.error(errorString);
            throw new ObjectIdDontMatchException(errorString);
        }

        // check if <ClinicalData> MetaDataVersionOID match provided <MetaDataVersion> OID
        if (!metaDataVersion.getOID().equals(clinicalData.getMetaDataVersionOID())) {
            String errorString = String.format("<ClinicalData> MetaDataVersionOID: %s does not " +
                                               "match provided <MetaDataVersion> OID: %s",
                    clinicalData.getMetaDataVersionOID(), metaDataVersion.getOID());
            LOGGER.error(errorString);
            throw new ObjectIdDontMatchException(errorString);
        }

        // Loop through all <StudyEventData> => QuestionnaireResponse
        if (subjectData.getStudyEventData().isEmpty()) {
            String error = "No <StudyEventData> Element found in <SubjectData>";
            LOGGER.error(error);
            throw new StudyEventDataNotFoundException(error);
        }

        // List of all <StudyEventData> Elements in one <SubjectData>
        // Each Element will be converted to a Single QuestionnaireResponse
        List<ODMcomplexTypeDefinitionStudyEventData> _studyEventData = subjectData.getStudyEventData();
        // HashMap with key (OID) and value (linkId)
        HashMap<String, String> formDefLinkIdMap = new HashMap<>();
        for (ODMcomplexTypeDefinitionStudyEventData studyEventData : _studyEventData) {
            LOGGER.info(
                    String.format("Converting <StudyEventData> with OID: %s",
                            studyEventData.getStudyEventOID())
            );
            // QuestionnaireResponse
            QuestionnaireResponse qrs = createQuestionnaireResponseBase();
            // Create root Element (linkId: 1, no "name")
            QuestionnaireResponse.QuestionnaireResponseItemComponent root = qrs.addItem();
            root.setLinkId("1");
            //List of <StudyEventDef> from <MetaDataVersion>
            List<ODMcomplexTypeDefinitionStudyEventDef> _studyEvents
                    = metaDataVersion.getStudyEventDef();
            LOGGER.info(
                    String.format("Searching for corresponding <StudyEventDef> in <MetaDataVersion>" +
                                  " for <StudyEventData> with OID: %s",
                            studyEventData.getStudyEventOID()));
            // Search for corresponding <StudyEventDef> in the <MetaDataVersion>
            Optional<ODMcomplexTypeDefinitionStudyEventDef> studyEventDef = _studyEvents.stream()
                    .filter(obj -> studyEventData.getStudyEventOID().equals(obj.getOID()))
                    .findFirst();
            if (studyEventDef.isPresent()) {
                LOGGER.info(
                        String.format("<StudyEventDef> with OID: %s found!",
                                studyEventDef.get().getOID()));
            } else {
                String error = String.format("No corresponding <StudyEventDef> with OID: %s found!",
                        studyEventData.getStudyEventOID());
                LOGGER.error(error);
                throw new NoCorrespondingStudyEventDefFoundException(error);
            }

            // List of <FormData> => linkId under the root (1.x)
            List<ODMcomplexTypeDefinitionFormData> _formData = studyEventData.getFormData();
            if (_formData.isEmpty()) {
                String error = String.format("No <FormData> found in <StudyEventData> with OID: %s",
                        studyEventData.getStudyEventOID());
                LOGGER.error(error);
                throw new FormDataNotFoundException(error);
            } else {
                LOGGER.info("<FormData> found in <StudyEventData>");
            }

            // <FormDef> list from <MetaDataVersion> this list
            // will be searched for each <FormData>
            List<ODMcomplexTypeDefinitionFormDef> _formDef = metaDataVersion.getFormDef();

            // Loop through all <FormData> Elements
            for (int i = 0; i < _formData.size(); i++) {

                // Current <FormData> Element
                ODMcomplexTypeDefinitionFormData formData = _formData.get(i);

                // Search for corresponding <FormDef> in the <MetaDataVersion>
                LOGGER.info(
                        String.format("Searching for corresponding " +
                                      "<FormDef> with OID: %s in <MetaDataVersion>",
                                formData.getFormOID()));
                Optional<ODMcomplexTypeDefinitionFormDef> formDef = _formDef.stream()
                        .filter(obj -> formData.getFormOID().equals(obj.getOID()))
                        .findFirst();

                if (formDef.isPresent()) {
                    LOGGER.info(
                            String.format("<FormDef> with OID: %s found!",
                                    formDef.get().getOID()));
                } else {
                    String error = String.format("No corresponding <FormDef> found for <FormData> with OID: %s",
                            formData.getFormOID());
                    LOGGER.error(error);
                    throw new NoCorrespondingFormDefFoundException(error);
                }

                // Create QRS Item one level under the root
                QuestionnaireResponse.QuestionnaireResponseItemComponent item_1_x = root.addItem();
                LOGGER.info(
                        String.format("Converting <FormData> with OID: %s",
                                formData.getFormOID()));


                // Set text of the item
                // WARNING MAY PRODUCE INDEX OUT OF BOUNDS EXCEPTION
                //ToDo: insert language check via parameter
                ODMcomplexTypeDefinitionDescription description = formDef.get().getDescription();
                if (description.getTranslatedText().isEmpty()) {
                    String error = String.format(
                            "No <TranslatedText> in <Description> found for <FormDef> with OID: %s!",
                            formDef.get().getOID()
                    );
                    LOGGER.error(error);
                    throw new FormDescriptionNotFoundException(error);
                } else {
                    LOGGER.info("Set text of linkId 1.x Element!");
                    item_1_x.setText(formDef.get().getDescription().getTranslatedText().get(0).getValue());
                }

                // Check if form is in HashMap if not => add
                if (formDefLinkIdMap.containsKey(formData.getFormOID())) {
                    LOGGER.info(String.format(
                            "LinkId for <FormData> with OID: %s found",
                            formData.getFormOID()
                    ));
                    // Set linkId from HashMap
                    item_1_x.setLinkId(String.format("1.%s", formDefLinkIdMap.get(formData.getFormOID())));
                } else {
                    LOGGER.info(String.format(
                            "Generate linkId for <FormData> with OID: %s",
                            formData.getFormOID()
                    ));
                    // Set append Value in HashMap
                    int linkId = (i + 1);
                    formDefLinkIdMap.put(formData.getFormOID(), String.valueOf(linkId));
                    // Get append Value in HashMap
                    item_1_x.setLinkId(String.format("1.%s", formDefLinkIdMap.get(formData.getFormOID())));
                }


            }

        }

        return "";
    }

    private Bundle createBundle() {
        return new Bundle();
    }

    private QuestionnaireResponse createQuestionnaireResponseBase() {
        QuestionnaireResponse qrs = new QuestionnaireResponse();
        qrs.setQuestionnaire("http://link_to_questionnaire");
        qrs.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        return qrs;
    }


    @Override
    public void printClinicalData(ODM odm) {
        LOGGER.info("Received ODM in converter service");
        List<ODMcomplexTypeDefinitionClinicalData> _clinicalData = odm.getClinicalData();
        List<ODMcomplexTypeDefinitionStudy> _study = odm.getStudy();
        ODMcomplexTypeDefinitionStudy study = null;
        ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion = null;
        if (_study.isEmpty()) {
            LOGGER.warn("No <Study> was found!");
        } else {
            LOGGER.info("Only <Study> at index 0 is picked!");
            study = _study.get(0);
            if (study.getMetaDataVersion().isEmpty()) {
                LOGGER.warn("No <MetaDataVersion> was found in <Study>");
            } else {
                LOGGER.info("Only <MetaDataVersion> at index 0 is picked!");
                metaDataVersion = study.getMetaDataVersion().get(0);
            }
        }
        // <ClinicalData> => FHIR Bundle
        if (!_clinicalData.isEmpty()) {
            LOGGER.info("Only <ClinicalData> element at index 0 is picked!");
            ODMcomplexTypeDefinitionClinicalData clinicalData = _clinicalData.get(0);
            List<ODMcomplexTypeDefinitionSubjectData> _subjectData = clinicalData.getSubjectData();
            if (!_subjectData.isEmpty()) {
                LOGGER.info("Only <SubjectData> from Patient at index 0 is picked!");
                ODMcomplexTypeDefinitionSubjectData subjectData = _subjectData.get(0);
                List<ODMcomplexTypeDefinitionStudyEventData> _studyEventData = subjectData.getStudyEventData();
                if (!_studyEventData.isEmpty()) {
                    LOGGER.info(String.format("Detected amount of <StudyEventData>: %d", _studyEventData.size()));
                    // Loop through all <StudyEventData> in <SubjectData> => QuestionnaireResponse
                    for (ODMcomplexTypeDefinitionStudyEventData studyEventData : _studyEventData) {
                        LOGGER.info(String.format("Looking into <StudyEventData> with OID: %s",
                                studyEventData.getStudyEventOID()));
                        if (metaDataVersion == null) {
                            LOGGER.warn("No <StudyEventDef> cloud be found because <MetaDataVersion> at index 0 was null!");
                        } else {
                            //List of <StudyEventDef> from <MetaDataVersion>
                            List<ODMcomplexTypeDefinitionStudyEventDef> _studyEvents
                                    = metaDataVersion.getStudyEventDef();
                            // search in the <StudyEventDef> list for the first definition
                            // with same OID as the <StudyEventData>
                            LOGGER.info(
                                    String.format("Searching for corresponding <StudyEventDef> in <MetaDataVersion>" +
                                                  " for <StudyEventData> with OID: %s",
                                            studyEventData.getStudyEventOID()));
                            Optional<ODMcomplexTypeDefinitionStudyEventDef> studyEventDef = _studyEvents.stream()
                                    .filter(obj -> studyEventData.getStudyEventOID().equals(obj.getOID()))
                                    .findFirst();
                            // <StudyEventDef> found!
                            if (studyEventDef.isPresent()) {
                                LOGGER.info(
                                        String.format("<StudyEventDef> with OID: %s found!",
                                                studyEventDef.get().getOID()));
                                // List of <FormData> in <StudyEventData> => 1.x
                                // <FormData> => link.id 1.x under the root Level in QRS
                                List<ODMcomplexTypeDefinitionFormData> _formData = studyEventData.getFormData();
                                if (!_formData.isEmpty()) {
                                    LOGGER.info("<FormData> found!");
                                    // <FormDef> list from <MetaDataVersion> this list
                                    // will be searched for each <FormData>
                                    List<ODMcomplexTypeDefinitionFormDef> _formDef = metaDataVersion.getFormDef();
                                    for (ODMcomplexTypeDefinitionFormData formData : _formData) {
                                        LOGGER.info(
                                                String.format("Looking into <FormData> with OID: %s",
                                                        formData.getFormOID()));
                                        // Search for corresponding <FormDef> in <MetaDataVersion>
                                        LOGGER.info(
                                                String.format("Searching for corresponding " +
                                                              "<FormDef> with OID: %s in <MetaDataVersion>",
                                                        formData.getFormOID()));
                                        Optional<ODMcomplexTypeDefinitionFormDef> formDef = _formDef.stream()
                                                .filter(obj -> formData.getFormOID().equals(obj.getOID()))
                                                .findFirst();
                                        // corresponding <FormDef> for <FormData> found!
                                        if (formDef.isPresent() && !_formDef.isEmpty()) {
                                            LOGGER.info(
                                                    String.format("<FormDef> with OID: %s found!",
                                                            formDef.get().getOID()));
                                            List<ODMcomplexTypeDefinitionItemGroupData> _itemGroupData =
                                                    formData.getItemGroupData();
                                            LOGGER.info(
                                                    String.format("Detected amount of <ItemGroupData> in " +
                                                                  "<FormData> with OID: %s (amount -> %d)",
                                                            formData.getFormOID(), _itemGroupData.size()));
                                            if (!_itemGroupData.isEmpty()) {
                                                // List of <ItemGroupDef> in <MetaDataVersion>
                                                List<ODMcomplexTypeDefinitionItemGroupDef> _itemGroupDef =
                                                        metaDataVersion.getItemGroupDef();
                                                // Loop through all <ItemGroupData>
                                                for (ODMcomplexTypeDefinitionItemGroupData itemGroupData :
                                                        _itemGroupData) {
                                                    LOGGER.info(
                                                            String.format("Looking into <ItemGroupData> with OID: %s",
                                                                    itemGroupData.getItemGroupOID()));
                                                    // Search for corresponding <ItemGroupDef> in <MetaDataVersion>
                                                    LOGGER.info(
                                                            String.format("Searching for corresponding <ItemGroupDef>" +
                                                                          " with OID: %s in <MetaDataVersion",
                                                                    itemGroupData.getItemGroupOID()));
                                                    Optional<ODMcomplexTypeDefinitionItemGroupDef> itemGroupDef =
                                                            _itemGroupDef.stream()
                                                                    .filter(obj -> itemGroupData.getItemGroupOID()
                                                                            .equals(obj.getOID()))
                                                                    .findFirst();
                                                    // Found corresponding <ItemGroupDef>
                                                    if (itemGroupDef.isPresent() && !_itemGroupDef.isEmpty()) {
                                                        LOGGER.info(String.format("<ItemGroupDef> with OID: %s found!",
                                                                itemGroupDef.get().getOID()));
                                                        // List of <ItemData> in the <ItemGroup>
                                                        List<ODMcomplexTypeDefinitionItemData> _itemData =
                                                                itemGroupData.getItemDataGroup();
                                                        LOGGER.info(
                                                                String.format("Detected amount of <ItemData> in " +
                                                                              "<ItemGroupData> with OID: %s (amount -> %d)",
                                                                        itemGroupData.getItemGroupOID(), _itemData.size()));
                                                        if (!_itemData.isEmpty()) {
                                                            //List of all <ItemDef> in <MetaDataVersion>
                                                            List<ODMcomplexTypeDefinitionItemDef> _itemDef =
                                                                    metaDataVersion.getItemDef();
                                                            // Loop through all <ItemData>
                                                            for (ODMcomplexTypeDefinitionItemData itemData :
                                                                    _itemData) {
                                                                LOGGER.info(
                                                                        String.format("Looking into <ItemData> " +
                                                                                      "with OID: %s",
                                                                                itemData.getItemOID())
                                                                );
                                                                // Search for the corresponding <ItemDef>
                                                                // in <MetaDataVersion>
                                                                Optional<ODMcomplexTypeDefinitionItemDef> itemDef =
                                                                        _itemDef.stream()
                                                                                .filter(obj -> itemData.getItemOID()
                                                                                        .equals(obj.getOID()))
                                                                                .findFirst();
                                                                // Found corresponding <ItemDef>
                                                                if (itemDef.isPresent() && !_itemDef.isEmpty()) {
                                                                    // INFO: just testing <Alias> assuming it's always at
                                                                    // index 0
                                                                    try {
                                                                        LOGGER.info(String.format(
                                                                                "ItemValue: %s ; OID: %s ; Alias: %s",
                                                                                itemData.getValue(),
                                                                                itemData.getItemOID(),
                                                                                itemDef.get().getAlias().get(0).getName()
                                                                        ));
                                                                    } catch (IndexOutOfBoundsException e) {
                                                                        LOGGER.warn("No <Alias> in <ItemDef>");
                                                                        LOGGER.info(String.format(
                                                                                "ItemValue: %s ; OID: %s",
                                                                                itemData.getValue(),
                                                                                itemData.getItemOID()
                                                                        ));
                                                                    }
                                                                } else {
                                                                    LOGGER.warn(
                                                                            String.format("No <ItemDef> was found " +
                                                                                          "in <MetaDataVersion> with OID: %s",
                                                                                    itemData.getItemOID()));
                                                                }
                                                            }
                                                        } else {
                                                            LOGGER.info(String.format("No <ItemData> was found " +
                                                                                      "in <ItemGroupData> with OID: %s",
                                                                    itemGroupData.getItemGroupOID()));
                                                        }
                                                    } else {
                                                        LOGGER.warn(
                                                                String.format("No <ItemGroupDef> was found " +
                                                                              "in <MetaDataVersion> with OID: %s",
                                                                        itemGroupData.getItemGroupOID()));
                                                    }
                                                }
                                            } else {
                                                LOGGER.warn(
                                                        String.format("No <ItemGroupData> was found " +
                                                                      "in <FormData> with OID: %s",
                                                                formData.getFormOID()));
                                            }
                                        } else {
                                            // No <FormDef> found in <MetaDataVersion>
                                            LOGGER.warn(
                                                    String.format("No corresponding <FormDef> found for <FormData> with OID: %s",
                                                            formData.getFormOID()));
                                        }
                                    }
                                } else {
                                    LOGGER.warn(
                                            String.format("No <FormData> found in <StudyEventData> with OID: %s",
                                                    studyEventData.getStudyEventOID()));
                                }

                            } else {
                                // <StudyEventDef> not found!
                                LOGGER.warn(
                                        String.format("No corresponding <StudyEventDef> with OID: %s found!",
                                                studyEventData.getStudyEventOID()));
                            }
                        }
                    }
                } else {
                    LOGGER.warn("No <StudyEventData> was found!");
                }
            } else {
                LOGGER.warn("No <SubjectData> was found!");
            }
        } else {
            LOGGER.warn("No <ClinicalData> element was found!");
        }
    }
}
