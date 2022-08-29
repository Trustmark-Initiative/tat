import liquibase.statement.core.UpdateStatement

databaseChangeLog = {

    changeSet(author: "rs239 (generated)", id: "1654871654492-1") {
        createTable(tableName: "trustmark_recipient_identifier") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "trustmark_recipient_identifierPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "default_trustmark_recipient_identifier", type: "BIT") {
                constraints(nullable: "false")
            }

            column(name: "organization_id", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "uri", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1654871654492-2") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "trustmark_recipient_identifier", constraintName: "FK4ai8d1takpm93wwu6ys3x81yh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", validate: "true")
    }

    changeSet(author: 'rs239 (generated)', id: 'update-org-trustmark-recipient-identifier-20114011-2')  {
        // At this point the trustmark reciipient identifier is empty. Fill the table from the organization
        sql("""INSERT INTO trustmark_recipient_identifier (uri, version, default_trustmark_recipient_identifier, organization_id) SELECT uri, 0, 1, id FROM organization;""")
    }
}
