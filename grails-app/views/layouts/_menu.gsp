<%@ page import="grails.plugin.springsecurity.SpringSecurityUtils" %>
<%@ page import="nstic.web.UserRole" %>
<%@ page import="nstic.web.Role" %>

<nav class="navbar navbar-inverse navbar-fixed-top">
<!--  <nav class="navbar navbar-default tatmenu" role="navigation">  -->
    <!-- Brand and toggle get grouped for better mobile display -->
    <div class="navbar-header">
        <button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#bs-example-navbar-collapse-1">
            <span class="sr-only">Toggle navigation</span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" target="_self" href="${createLink(uri:'/')}">
            ${grailsApplication.config.tf.tool.name}
        </a>
    </div>

    <!-- Collect the nav links, forms, and other content for toggling -->
    <div class="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
        <ul class="nav navbar-nav navbar-center">
            <sec:ifLoggedIn>
                <li>
                    <a href="${createLink(uri:'/')}">
                        <span class="glyphicon glyphicon-home"></span>
                        Home
                    </a>
                </li>
                <sec:ifAllGranted roles="ROLE_USER">
                    <li class="dropdown">
                        <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                            <span class="glyphicon glyphicon-list"></span>
                            Manage <b class="caret"></b>
                        </a>
                        <ul class="dropdown-menu" role="menu">
                            <li><a href="${createLink(controller:'assessment', action:'list')}">Assessments</a></li>
                            <li><a href="${createLink(controller: 'contactInformation', action: 'list')}" title="Manage Contacts">Contacts</a></li>
                            <li><a href="${createLink(controller: 'organization', action: 'list')}" title="Manage Organizations">Organizations</a></li>
                            <li><a href="${createLink(controller: 'documents', action: 'list')}" title="Manage Documents">Documents</a></li>
                            <li><a href="${createLink(controller:'tip', action:'list')}">Trust Interoperability Profiles</a></li>
                            <li><a href="${createLink(controller:'trustmark', action:'list')}">Trustmarks</a></li>
                            <li><a href="${createLink(controller:'trustmarkDefinition', action:'list')}">Trustmark Definitions</a></li>
                            <li><a href="${createLink(controller:'trustmarkMetadata', action:'list')}">Trustmark Metadata</a></li>
                            <sec:ifAllGranted roles="ROLE_ADMIN">
                                <li><a href="${createLink(controller: 'user', action: 'list')}" title="Manage User Accounts">Users</a></li>
                            </sec:ifAllGranted>
                        </ul>
                    </li>
                </sec:ifAllGranted>
                <li>
                    <a href="${createLink(controller:'profile', id: sec.username())}" title="Manage Your User Profile">
                        <span class="glyphicon glyphicon-user"></span>
                        Profile
                    </a>
                </li>
                <sec:ifAllGranted roles="ROLE_USER">
                    <li>
                        <a href="${createLink(controller:'reports', action: 'organizationReport')}" title="Generate Organizational Reports">
                            <span class="glyphicon glyphicon-stats"></span>
                            Organizational Reports
                        </a>
                    </li>
                </sec:ifAllGranted>
            </sec:ifLoggedIn>
            <sec:ifAllGranted roles="ROLE_ADMIN">
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <span class="glyphicon glyphicon-list-alt"></span>
                        Administration <b class="caret"></b>
                    </a>
                    <ul class="dropdown-menu" role="menu">
                        <li><a href="${createLink(controller: 'admin', action: 'importExportView')}">Import/Export</a></li>
                        <li><a href="${createLink(controller: 'error')}">Error Tests</a></li>
                        <li><a href="${createLink(controller: 'tdAndTipUpdate')}">TPAT Management</a></li>
                        <li><a href="${createLink(controller: 'email', action: 'settings')}">Email</a></li>
                    </ul>
                </li>
            </sec:ifAllGranted>
            <sec:ifLoggedIn>
                <li><a href="${createLink(controller: 'logout')}">Logout</a></li>
            </sec:ifLoggedIn>
            <sec:ifNotLoggedIn>
                <li class="dropdown">
                    <a href="#" class="dropdown-toggle" data-toggle="dropdown">
                        <span class="glyphicon glyphicon-list"></span>
                        Hosted Artifacts <b class="caret"></b>
                    </a>
                    <ul class="dropdown-menu" role="menu">
                        <li><a href="${createLink(controller: 'publicApi', action: 'documents')}">Documents</a></li>
                        <li><a href="${createLink(controller: 'publicApi', action: 'trustmarks')}">Trustmarks</a></li>
                    </ul>
                </li>
                <g:if test="${(UserRole.countByRole(Role.findByAuthority(Role.ROLE_ADMIN)) != 0)}">
                    <li><a href="${createLink(controller: 'login')}">Login</a></li>
                </g:if>
            </sec:ifNotLoggedIn>
        </ul>
    </div><!-- /.navbar-collapse -->
</nav>
