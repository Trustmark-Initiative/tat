databaseChangeLog = {

    changeSet(author: "rs239 (generated)", id: "1655235989871-1") {
        addColumn(tableName: "trustmark") {
            column(name: "trustmark_recipient_identifier_ref", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    //
    changeSet(author: 'rs239 (generated)', id: 'update--trustmark-for-trustmark-recipient-identifiers-20114011-3') {
        sql("""UPDATE trustmark SET trustmark.trustmark_recipient_identifier_ref = ( SELECT trustmark_recipient_identifier.id FROM trustmark_recipient_identifier JOIN organization WHERE trustmark_recipient_identifier.uri = organization.uri AND organization.id = trustmark.recipient_organization_ref);""")
    }

    // Need to add the constraint after the table is updated
    changeSet(author: "rs239 (generated)", id: "1655235989871-2") {
        addForeignKeyConstraint(baseColumnNames: "trustmark_recipient_identifier_ref", baseTableName: "trustmark", constraintName: "FKi72yssl2w377bj857m9k0feyn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trustmark_recipient_identifier", validate: "true")
    }

}
