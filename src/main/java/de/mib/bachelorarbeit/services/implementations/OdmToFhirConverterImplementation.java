package de.mib.bachelorarbeit.services.implementations;


import ca.uhn.fhir.context.FhirContext;
import de.mib.bachelorarbeit.services.definitions.OdmToFhirConverter;
import odm.*;
import org.hl7.fhir.r4.model.Patient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
