package nstic.td.v1_0.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Calendar;

@XmlRootElement(name="TrustmarkDefinition", namespace=XmlConstants.NAMESPACE_PREFIX)
public class TrustmarkDefinition {
    //==================================================================================================================
    //  Instance Variables
    //==================================================================================================================
    private String id;
    private String name;
    private String version;
    private String description;
    private Calendar publicationTimestamp;
    private String publicationUrl;
    private String targetStakeholderDescription;
    private String targetTrustmarkRecipientDescription;
    private String targetTrustmarkRelyingPartyDescription;
    private String targetTrustmarkProviderDescription;
    //==================================================================================================================
    //  Getters
    //==================================================================================================================
    @XmlElement(name="TrustmarkDefinitionID", required = true, namespace = XmlConstants.NAMESPACE_PREFIX)
    public String getId() {
        return id;
    }

    @XmlElement(name="TrustmarkDefinitionName", required = true, namespace = XmlConstants.NAMESPACE_PREFIX)
    public String getName() {
        return name;
    }

    @XmlElement(name="TrustmarkDefinitionVersionText", required = true, namespace = XmlConstants.NAMESPACE_PREFIX)
    public String getVersion() {
        return version;
    }

    @XmlElement(name="TrustmarkDefinitionDescriptionText", required = true, namespace = XmlConstants.NAMESPACE_PREFIX)
    public String getDescription() {
        return description;
    }

    @XmlElement(name="TrustmarkDefinitionPublicationDateTime", required = true, namespace = XmlConstants.NAMESPACE_PREFIX)
    public Calendar getPublicationTimestamp() {
        return publicationTimestamp;
    }

    @XmlElement(name="TrustmarkDefinitionPublicationURL", required = false, namespace = XmlConstants.NAMESPACE_PREFIX)
    public String getPublicationUrl() {
        return publicationUrl;
    }

    @XmlElement(name="TrustmarkDefinitionStakeholderDescriptionText", required = false, namespace = XmlConstants.NAMESPACE_PREFIX)
    public String getTargetStakeholderDescription() {
        return targetStakeholderDescription;
    }

    @XmlElement(name="TrustmarkDefinitionRecipientDescriptionText", required = false, namespace = XmlConstants.NAMESPACE_PREFIX)
    public String getTargetTrustmarkRecipientDescription() {
        return targetTrustmarkRecipientDescription;
    }

    @XmlElement(name="TrustmarkDefinitionRelyingPartyDescriptionText", required = false, namespace = XmlConstants.NAMESPACE_PREFIX)
    public String getTargetTrustmarkRelyingPartyDescription() {
        return targetTrustmarkRelyingPartyDescription;
    }

    @XmlElement(name="TrustmarkDefinitionProviderDescriptionText", required = false, namespace = XmlConstants.NAMESPACE_PREFIX)
    public String getTargetTrustmarkProviderDescription() {
        return targetTrustmarkProviderDescription;
    }
    //==================================================================================================================
    //  Setters
    //==================================================================================================================
    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setPublicationTimestamp(Calendar publicationTimestamp) {
        this.publicationTimestamp = publicationTimestamp;
    }
    public void setPublicationUrl(String publicationUrl) {
        this.publicationUrl = publicationUrl;
    }
    public void setTargetStakeholderDescription(String targetStakeholderDescription) {
        this.targetStakeholderDescription = targetStakeholderDescription;
    }
    public void setTargetTrustmarkRecipientDescription(String targetTrustmarkRecipientDescription) {
        this.targetTrustmarkRecipientDescription = targetTrustmarkRecipientDescription;
    }
    public void setTargetTrustmarkRelyingPartyDescription(String targetTrustmarkRelyingPartyDescription) {
        this.targetTrustmarkRelyingPartyDescription = targetTrustmarkRelyingPartyDescription;
    }
    public void setTargetTrustmarkProviderDescription(String targetTrustmarkProviderDescription) {
        this.targetTrustmarkProviderDescription = targetTrustmarkProviderDescription;
    }
    //==================================================================================================================
    //  Constructors
    //==================================================================================================================

    //==================================================================================================================
    //  Private Methods
    //==================================================================================================================

    //==================================================================================================================
    //  Protected Methods
    //==================================================================================================================

    //==================================================================================================================
    //  Public Methods
    //==================================================================================================================

}//end TrustmarkDefinition()
