
databaseChangeLog = {

    changeSet(author: "rs239 (generated)", id: "1654624064936-1") {
        addColumn(tableName: "organization") {
            column(name: "is_trustmark_provider", type: "bit") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: 'rs239 (generated)', id: 'update-org-trustmark-provider-20114011-1')  {
        // Update the first record as a trustmark provider since it is the first organization
        // created from config
        sql("""UPDATE organization SET organization.is_trustmark_provider = 1 WHERE id = 1;""")
    }

    changeSet(author: 'rs239 (generated)', id: 'update-org-trustmark-provider-20114011-2')  {
        // update the rest of the records as non-trustmark providers
        sql("""UPDATE organization SET organization.is_trustmark_provider = 0 WHERE id > 1;""")
    }
}
