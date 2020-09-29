<%-- A partial page to show the user the export turstmark list. --%>
<div>
    <table class="table table-striped table-bordered table-condensed">
        <thead>
            <tr>
                <th>&nbsp;</th>
                <th>Identifier</th>
                <th>Status</th>
                <th>Expiration Date</th>
                <th>Granted By</th>
                <th>Trustmark Definition</th>
                <th>Recipient Organization</th>
            </tr>
        </thead>
        <tbody>
            <g:if test="${trustmarks && trustmarks.size() > 0}">
                <g:each in="${trustmarks}" var="trustmark">
                    <tr>
                        <td><input class="trustmarkExportCheckbox" id="tm_${trustmark.id}" type="checkbox" /></td>
                        <td>
                            <g:link controller="trustmark" action="view" id="${trustmark.identifier}">
                                ${trustmark.identifier}
                            </g:link>
                        </td>
                        <td style="text-align: center;">
                            <g:if test="${trustmark.status == nstic.web.assessment.TrustmarkStatus.OK}">
                                <span style="color: darkgreen;" class="glyphicon glyphicon-ok-sign" title="Trustmark still valid"></span>
                            </g:if>
                            <g:if test="${trustmark.status == nstic.web.assessment.TrustmarkStatus.REVOKED}">
                                <span style="color: darkred;" class="glyphicon glyphicon-remove-sign" title="Trustmark has been revoked."></span>
                            </g:if>
                            <g:if test="${trustmark.status == nstic.web.assessment.TrustmarkStatus.EXPIRED}">
                                <span style="color: rgb(150, 150, 0);" class="glyphicon glyphicon-minus-sign" title="Trustmark has expired."></span>
                            </g:if>
                        </td>
                        <td>
                            <g:formatDate format="yyyy-MM-dd" date="${trustmark.expirationDateTime}" />
                        </td>
                        <td>
                            ${trustmark.grantingUser.contactInformation.responder}
                        </td>
                        <td>
                            ${trustmark.trustmarkDefinition.name}
                        </td>
                        <td>${trustmark.recipientOrganization.name}</td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr>
                    <td colspan="6"><em>There are no trustmarks.</em></td>
                </tr>
            </g:else>
        </tbody>
    </table>

</div>