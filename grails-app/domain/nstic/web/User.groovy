package nstic.web

import grails.plugin.springsecurity.SpringSecurityService

class User {

    transient SpringSecurityService springSecurityService

    final String NOOP = "{noop}"
	String username
	String password
	boolean enabled = true
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired

    // My custom additions...  Taken from contact information in Trustmark Definition XML (as of 2014-03-24)
    ContactInformation contactInformation
    Organization organization

	static transients = ['springSecurityService']

	static constraints = {
		username blank: false, unique: true
		password blank: false
        contactInformation null: false
        organization(null: false)
	}

	static mapping = {
        table name: 'assessment_user'
		password column: '`password`'
        organization column: 'organization_ref'
        contactInformation column: 'contact_information_ref'
	}

    public Boolean isAdmin() {
        Set<UserRole> roles = UserRole.findAllByUser(this)
        boolean hasRole = false
        roles.each { UserRole role ->
            if( role.role.authority == Role.ROLE_ADMIN )
                hasRole = true
        }
        return hasRole
    }

    public Boolean isUser() {
        Set<UserRole> roles = UserRole.findAllByUser(this)
        boolean hasRole = false
        roles.each { UserRole role ->
            if( role.role.authority == Role.ROLE_USER )
                hasRole = true
        }
        return hasRole
    }


    public Boolean isReportOnly() {
        Set<UserRole> roles = UserRole.findAllByUser(this)
        boolean hasRole = false
        roles.each { UserRole role ->
            if( role.role.authority == Role.ROLE_REPORTS_ONLY )
                hasRole = true
        }
        return hasRole
    }


    Set<Role> getAuthorities() {
		UserRole.findAllByUser(this).collect { it.role } as Set<Role>
	}

	def beforeInsert() {
		encodePassword()
	}

	def beforeUpdate() {
		if (isDirty('password')) {
			encodePassword()
		}
	}

	protected void encodePassword() {
        password = springSecurityService?.passwordEncoder ?
                springSecurityService.encodePassword(NOOP+password) :
                NOOP+password
	}

    String toString() {
        return username
    }

    public Map toJsonMap(boolean shallow = true) {
        def json = [
                id: this.id,
                username: this.username,
                enabled: this.enabled,
                contactInformation: this.contactInformation?.toJsonMap(shallow),
                organization: this.organization?.toJsonMap(shallow)
        ] as java.lang.Object
        if( !shallow ){
            // TODO Create rest of data model...
        }
        return json as Map
    }//end toJsonMap

}//end User
