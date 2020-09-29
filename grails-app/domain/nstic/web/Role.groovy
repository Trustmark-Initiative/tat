package nstic.web

class Role {

    static String ROLE_REPORTS_ONLY = "ROLE_REPORTS_ONLY"
	static String ROLE_USER = "ROLE_USER"
    static String ROLE_ADMIN = "ROLE_ADMIN"

	static List<String> ALL_ROLES = [ROLE_REPORTS_ONLY, ROLE_USER, ROLE_ADMIN]

	Role(){}
	Role(String authority) {
		this()
		this.authority = authority
	}

	String authority

	static mapping = {
		cache true
	}

	static constraints = {
		authority blank: false, unique: true
	}
}
