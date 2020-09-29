package nstic.web

class Registry {

    String name
    String registryUrl
    Date lastUpdated

    static constraints = {
        name(nullable: false, blank: false, maxSize: 254)
        registryUrl(nullable: false, blank: false, maxSize: 512)
        lastUpdated(nullable: false)
    }

    static mapping = {
        table 'system_registry'
        name column: 'registry_name'
        registryUrl type: 'text', column: 'registry_url'
        lastUpdated type: 'date', column: 'last_update'
    }
}
