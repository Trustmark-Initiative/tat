databaseChangeLog = {
    changeSet(author: "rs239 (generated)", id: "1636063530707-160") {
        dropNotNullConstraint(columnDataType: "VARCHAR(255)", columnName: "state_or_province_name", tableName: "signing_certificates")
        dropNotNullConstraint(columnDataType: "VARCHAR(255)", columnName: "organization_name", tableName: "signing_certificates")
        dropNotNullConstraint(columnDataType: "VARCHAR(255)", columnName: "country_name", tableName: "signing_certificates")
        dropNotNullConstraint(columnDataType: "VARCHAR(255)", columnName: "locality_name", tableName: "signing_certificates")
        dropNotNullConstraint(columnDataType: "VARCHAR(255)", columnName: "organizational_unit_name", tableName: "signing_certificates")

    }
}
