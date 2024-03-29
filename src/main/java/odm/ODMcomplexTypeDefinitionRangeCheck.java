//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2023.11.23 at 03:20:02 PM CET 
//


package odm;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.*;


/**
 * <p>Java class for ODMcomplexTypeDefinition-RangeCheck complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ODMcomplexTypeDefinition-RangeCheck">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;choice>
 *           &lt;element ref="{http://www.cdisc.org/ns/odm/v1.3}CheckValue" maxOccurs="unbounded"/>
 *           &lt;element ref="{http://www.cdisc.org/ns/odm/v1.3}FormalExpression" maxOccurs="unbounded"/>
 *         &lt;/choice>
 *         &lt;element ref="{http://www.cdisc.org/ns/odm/v1.3}MeasurementUnitRef" minOccurs="0"/>
 *         &lt;element ref="{http://www.cdisc.org/ns/odm/v1.3}ErrorMessage" minOccurs="0"/>
 *         &lt;group ref="{http://www.cdisc.org/ns/odm/v1.3}RangeCheckElementExtension" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.cdisc.org/ns/odm/v1.3}RangeCheckAttributeDefinition"/>
 *       &lt;attGroup ref="{http://www.cdisc.org/ns/odm/v1.3}RangeCheckAttributeExtension"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ODMcomplexTypeDefinition-RangeCheck", propOrder = {
    "checkValue",
    "formalExpression",
    "measurementUnitRef",
    "errorMessage"
})
public class ODMcomplexTypeDefinitionRangeCheck {

    @XmlElement(name = "CheckValue")
    protected List<ODMcomplexTypeDefinitionCheckValue> checkValue;
    @XmlElement(name = "FormalExpression")
    protected List<ODMcomplexTypeDefinitionFormalExpression> formalExpression;
    @XmlElement(name = "MeasurementUnitRef")
    protected ODMcomplexTypeDefinitionMeasurementUnitRef measurementUnitRef;
    @XmlElement(name = "ErrorMessage")
    protected ODMcomplexTypeDefinitionErrorMessage errorMessage;
    @XmlAttribute(name = "Comparator")
    protected Comparator comparator;
    @XmlAttribute(name = "SoftHard", required = true)
    protected SoftOrHard softHard;

    /**
     * Gets the value of the checkValue property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the checkValue property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCheckValue().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ODMcomplexTypeDefinitionCheckValue }
     * 
     * 
     */
    public List<ODMcomplexTypeDefinitionCheckValue> getCheckValue() {
        if (checkValue == null) {
            checkValue = new ArrayList<ODMcomplexTypeDefinitionCheckValue>();
        }
        return this.checkValue;
    }

    /**
     * Gets the value of the formalExpression property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the formalExpression property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFormalExpression().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ODMcomplexTypeDefinitionFormalExpression }
     * 
     * 
     */
    public List<ODMcomplexTypeDefinitionFormalExpression> getFormalExpression() {
        if (formalExpression == null) {
            formalExpression = new ArrayList<ODMcomplexTypeDefinitionFormalExpression>();
        }
        return this.formalExpression;
    }

    /**
     * Gets the value of the measurementUnitRef property.
     * 
     * @return
     *     possible object is
     *     {@link ODMcomplexTypeDefinitionMeasurementUnitRef }
     *     
     */
    public ODMcomplexTypeDefinitionMeasurementUnitRef getMeasurementUnitRef() {
        return measurementUnitRef;
    }

    /**
     * Sets the value of the measurementUnitRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link ODMcomplexTypeDefinitionMeasurementUnitRef }
     *     
     */
    public void setMeasurementUnitRef(ODMcomplexTypeDefinitionMeasurementUnitRef value) {
        this.measurementUnitRef = value;
    }

    /**
     * Gets the value of the errorMessage property.
     * 
     * @return
     *     possible object is
     *     {@link ODMcomplexTypeDefinitionErrorMessage }
     *     
     */
    public ODMcomplexTypeDefinitionErrorMessage getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the value of the errorMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link ODMcomplexTypeDefinitionErrorMessage }
     *     
     */
    public void setErrorMessage(ODMcomplexTypeDefinitionErrorMessage value) {
        this.errorMessage = value;
    }

    /**
     * Gets the value of the comparator property.
     * 
     * @return
     *     possible object is
     *     {@link Comparator }
     *     
     */
    public Comparator getComparator() {
        return comparator;
    }

    /**
     * Sets the value of the comparator property.
     * 
     * @param value
     *     allowed object is
     *     {@link Comparator }
     *     
     */
    public void setComparator(Comparator value) {
        this.comparator = value;
    }

    /**
     * Gets the value of the softHard property.
     * 
     * @return
     *     possible object is
     *     {@link SoftOrHard }
     *     
     */
    public SoftOrHard getSoftHard() {
        return softHard;
    }

    /**
     * Sets the value of the softHard property.
     * 
     * @param value
     *     allowed object is
     *     {@link SoftOrHard }
     *     
     */
    public void setSoftHard(SoftOrHard value) {
        this.softHard = value;
    }

}
