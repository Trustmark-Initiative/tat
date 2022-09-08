<!DOCTYPE html>
<html>
	<head>
		<meta name="layout" content="main"/>
		<title>Edit Organization</title>

        <style type="text/css">

        </style>
	</head>
	<body>
        %{--<ol class="breadcrumb">--}%
            %{--<li class="active">Home</li>--}%
        %{--</ol>--}%

        <h1>Edit Organization</h1>
        <div class="pageSubsection">

        </div>

        <g:hasErrors bean="${orgCommand}">
            <div style="margin-top: 2em;" class="alert alert-danger">
                <g:renderErrors bean="${orgCommand}" />
            </div>
        </g:hasErrors>


        <div class="pageContent">
            <g:form class="form-horizontal" name="editOrganizationForm" method="POST" action="update">
                <g:hiddenField name="id" id="id" value="${orgCommand?.id}" />
                <fieldset>
                    <legend>Organization Information</legend>

                    <div class="form-group">
                        <label for="name" class="col-sm-2 control-label">Name</label>
                        <div class="col-sm-10">
                            <g:textField name="name" id="name" class="form-control" placeholder="Name" value="${orgCommand?.name}" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="identifier" class="col-sm-2 control-label">Abbreviation</label>
                        <div class="col-sm-10">
                            <g:textField name="identifier" id="identifier" class="form-control" placeholder="ABBR" value="${orgCommand?.identifier}" />
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="uri" class="col-sm-2 control-label">Trustmark Recipient Identifiers</label>
                        <div class="col-sm-10">
                            <div id="trustmark-recipient-identifiers-list"></div>
                            <div id="trustmark-recipient-identifiers-status"></div>
                            <div id="trustmark-recipient-identifiers-details"></div>

                            <script type="text/javascript">

                                $(document).ready(function(){

                                    getTrustmarkRecipientIdentifiers(${organization.id});
                                })

                                // Trustmark Recipient Identifiers

                                let getTrustmarkRecipientIdentifiers = function(oid) {
                                    list("${createLink(controller:'organization', action: 'trustmarkRecipientIdentifiers')}"
                                        , trustmarkRecipientIdentifierResults
                                        , { oid: oid }
                                    );
                                    hideIt('trustmark-recipient-identifiers-details');
                                }

                                let addTrustmarkRecipientIdentifier = function(trustmarkRecipientIdentifier)  {
                                    if(checkTrustmarkRecipientIdentifier(trustmarkRecipientIdentifier)) {
                                        add("${createLink(controller:'organization', action: 'addTrustmarkRecipientIdentifier')}"
                                            , function (data) {
                                                setStatusMessage('trustmark-recipient-identifiers-status', data);

                                                getTrustmarkRecipientIdentifiers(${organization.id});
                                            }
                                            , {
                                                orgid: ${organization.id}
                                                , identifier: trustmarkRecipientIdentifier
                                            }
                                        );
                                    }
                                }

                                let removeTrustmarkRecipientIdentifiers = function(oid)  {
                                    getCheckedIds('edit-trustmarkRecipientIdentifier', function(list) {
                                        update("${createLink(controller:'organization', action: 'deleteTrustmarkRecipientIdentifiers')}"
                                            , function (data){getTrustmarkRecipientIdentifiers(oid);}
                                            , { ids: list, orgid: oid }
                                        );
                                    });
                                }

                                let deleteTrustmarkRecipientIdentifier = function(tmrid)  {
                                    add("${createLink(controller:'organization', action: 'deleteTrustmarkRecipientIdentifier')}"
                                        , function(data){getTrustmarkRecipientIdentifiers(${organization.id});}
                                        , {
                                            orgid: ${organization.id}
                                            , tmrid: tmrid
                                        }
                                    );
                                }

                                let trustmarkRecipientIdentifierResults = function(results)  {
                                    renderTrustmarkRecipientIdentifiersOffset = curriedTrustmarkRecipientIdentifier('trustmark-recipient-identifiers-list')
                                    ({
                                        editable: results.editable
                                        , fnAdd: function(){renderTrustmarkRecipientIdentifiersForm('trustmark-recipient-identifiers-details'
                                            , populateTrustmarkRecipientIdentifiersForm
                                            , function(){
                                                addTrustmarkRecipientIdentifier(document.getElementById('trustmarkRecipientIdentifier').value);}, {id:0})}
                                        , fnRemove: function(){removeTrustmarkRecipientIdentifiers('${organization.id}');}
                                        , fnDraw: drawTrustmarkRecipientIdentifier
                                        , title: 'Trustmark Recipient Identifiers'
                                        , hRef: 'javascript:getTrustmarkRecipientIdentifierDetails'
                                    })
                                    (results)
                                    renderTrustmarkRecipientIdentifiersOffset(0);
                                }

                                let populateTrustmarkRecipientIdentifiersForm = function(trustmarkRecipientIdentifier) {

                                    if(trustmarkRecipientIdentifier.id !== 0) {
                                        document.getElementById('trustmarkRecipientIdentifier').value = trustmarkRecipientIdentifier.uri;
                                        document.getElementById('trustmarkRecipientIdentifier').focus();
                                    }
                                }

                                let getTrustmarkRecipientIdentifierDetails = function(id)  {
                                    get("${createLink(controller:'organization', action: 'getTrustmarkRecipientIdentifier')}"
                                        , trustmarkRecipientIdentifierDetail('trustmark-recipient-identifiers-details')(populateTrustmarkRecipientIdentifiersForm)
                                        (function(){updateTrustmarkRecipientIdentifier(id, document.getElementById('trustmarkRecipientIdentifier').value
                                            , ${organization.id});})
                                        , { orgid: ${organization.id}, rid:id }
                                    );
                                }

                                let updateTrustmarkRecipientIdentifier = function(id, trustmarkRecipientIdentifier, orgId)  {

                                    if(checkTrustmarkRecipientIdentifier(trustmarkRecipientIdentifier))  {
                                        update("${createLink(controller:'organization', action: 'updateTrustmarkRecipientIdentifier')}"
                                            , function(data){getTrustmarkRecipientIdentifiers(${organization.id});}
                                            , {
                                                id: id
                                                , trustmarkRecipientIdentifier: trustmarkRecipientIdentifier
                                                , organizationId: ${organization.id}
                                            });
                                    } else {
                                        scroll(0,0);
                                    }
                                }

                            </script>

                        </div>
                    </div>

                </fieldset>
                <fieldset>
                    <legend>Primary Contact Information</legend>

                    <div class="form-group">
                        <label for="email" class="col-sm-2 control-label">Email address</label>
                        <div class="col-sm-10">
                            <g:textField name="email" id="email" class="form-control" placeholder="user@example.com" value="${orgCommand?.email}" />
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="responder" class="col-sm-2 control-label">Responder Name</label>
                        <div class="col-sm-10">
                            <g:textField name="responder" id="responder" class="form-control" placeholder="Name" value="${orgCommand?.responder}" />
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="phone" class="col-sm-2 control-label">Phone Number</label>
                        <div class="col-sm-10">
                            <g:textField name="phone" id="phone" class="form-control" placeholder="555-555-5555" value="${orgCommand?.phone}" />
                        </div>
                    </div>
                    <div class="form-group" >
                        <label for="mailingAddress" class="col-sm-2 control-label">Mailing address</label>
                        <div class="col-sm-10">
                            <g:textField name="mailingAddress" id="mailingAddress" class="form-control" placeholder="Address Full Text" value="${orgCommand?.mailingAddress}" />
                        </div>
                    </div>
                    <div class="form-group" >
                        <label for="notes" class="col-sm-2 control-label">Notes</label>
                        <div class="col-sm-10">
                            <g:textField name="notes" id="notes" class="form-control" placeholder="" value="${orgCommand?.notes}" />
                        </div>
                    </div>

                </fieldset>

                <div class="form-group">
                    <div class="col-sm-offset-2 col-sm-10">
                        <button type="submit" class="btn btn-primary">Update</button>
                        <a href="${createLink(controller:'organization', action: 'list')}" class="btn btn-default">Cancel</a>
                    </div>
                </div>
            </g:form>

        </div>

	</body>
</html>
