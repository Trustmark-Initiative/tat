<%@ page contentType="text/html"%>

<!-- This Template recursively builds the HTML for a TIP Tree so that JQuery Tree Table will work. -->
<!--
  It is assumed there is an attribute called "data" passed to this template, which is an instance of nstic.util.TipTreeNode
-->

<g:if test="${data.parent != null}">
    <%
        StringBuilder ancestorClassList = new StringBuilder()
        def parent = data.parent;
        while( parent != null ){
            ancestorClassList.append("childOf"+parent.uniqueId+" ");
            parent = parent.parent;
        }
        request.setAttribute("ancestorClassList", ancestorClassList.toString());
    %>
<tr id="${data.uniqueId}" class="tr-hide <%= ancestorClassList %>">
</g:if><g:else>
<tr id="${data.uniqueId}">
</g:else>
<td>
        <span style="margin-left: 0; padding-left: 0;" class="tipcaret"></span>
        <span style="margin-left: 0; padding-left: 0;" class="glyphicon glyphicon-list" title="Trust Interoperability Profile"></span>
        <span style="margin-left: 0; padding-left: 0;">
            ${data.name}
            <g:if test="${data.localId != null}"><span class="tm-link">[${data.localId}]</span></g:if>
        </span>
        <a class="te-link" onclick="javascript:toggleTE(this);">Trust Expression</a><div class="no-trust-expression">${data.trustExpression}</div>

        <g:if test="${data.trustExpression.toLowerCase().contains(' or ')}">
            <span style="color: rgb(150, 0, 0); margin-left: 0; padding-left: 0;" class="glyphicon glyphicon-info-sign" title="This TIP's trust expression contains an 'OR'"></span>
        </g:if>
    </td>
    <td style="width: 7%; text-align: center;">${data.version}</td>
    <td style="width: 4%; text-align: center;"><a title="Click here to toggle the checkboxes on all children of ${data.name}." href="javascript:toggleAll('${data.uniqueId}');"><span style="margin-left: 0; padding-left: 0;"  class="glyphicon glyphicon-check"></span></a></td>
    <td style="width: 4%; text-align: center;"><a href="${data.uri}" target="_blank">HTML</a></td>
</tr>

<g:each in="${data.children}" var="nextTipData">
    <tmpl:recursivelyBuildTipHtml data="${nextTipData}" />
</g:each>

<g:each in="${data.trustmarkDefinitionReferences}" var="td">
    <%
        StringBuilder tdAncestorClassList = new StringBuilder()
        def parent2 = td.parent;
        while( parent2 != null ){
            tdAncestorClassList.append("childOf"+parent2.uniqueId+" ");
            parent2 = parent2.parent;
        }
        request.setAttribute("tdAncestorClassList", tdAncestorClassList.toString());
    %>
    <tr class="tr-hide <%= tdAncestorClassList %>">
        <td>
            <div class="td-block">
                <span class="glyphicon glyphicon-tag" title="Trustmark Definition"></span>
                <input type="checkbox" checked="checked" id="tdCheckbox${td.databaseId}-${td.ownerTipId}" name="tdCheckbox${td.databaseId}-${td.ownerTipId}" class="tdCheckbox" onchange="updateCheckboxCount(this);" />
            </div>
            <div style="display: inline-block;">${td.name}<br><span class="tm-link">[${td.localId}]</span></div>
        </td>
        <td style="width: 7%; text-align: center;">${td.version}</td>
        <td style="width: 4%; text-align: center;">&nbsp;</td>
        <td style="width: 4%; text-align: center;">
            <a href="${td.uri}" target="_blank">HTML</a>
        </td>
    </tr>
</g:each>
