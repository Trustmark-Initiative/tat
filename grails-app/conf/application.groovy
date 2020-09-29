
// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.securityConfigType = "Annotation"
grails.plugin.springsecurity.userLookup.userDomainClassName = 'nstic.web.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'nstic.web.UserRole'
grails.plugin.springsecurity.authority.className = 'nstic.web.Role'

grails.plugin.springsecurity.controllerAnnotations.staticRules = [
        [pattern: '/console/**', access: ['ROLE_ADMIN']],
        [pattern: '/monitoring/**', access: ['ROLE_ADMIN']],
        [pattern: '/**', access: ['permitAll']]
]

grails.plugin.springsecurity.filterChain.chainMap = [
	[pattern: '/assets/**',      filters: 'none'],
	[pattern: '/**/js/**',       filters: 'none'],
	[pattern: '/**/css/**',      filters: 'none'],
	[pattern: '/**/images/**',   filters: 'none'],
	[pattern: '/**/favicon.ico', filters: 'none'],
	[pattern: '/**',             filters: 'JOINED_FILTERS']
]

grails.plugin.springsecurity.useBasicAuth = true
grails.plugin.springsecurity.basic.realmName = "GTRI Assessment Tool"

// Requires 1 or more beans in resources.groovy which implement org.springframework.context.ApplicationListener
grails.plugin.springsecurity.useSecurityEventListener = true
