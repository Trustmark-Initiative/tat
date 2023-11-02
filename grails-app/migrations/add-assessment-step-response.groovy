databaseChangeLog = {

    changeSet(author: "rs239 (generated)", id: "1698181368829-1") {
        createTable(tableName: "assessment_step_response") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "assessment_step_responsePK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "result", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "is_default", type: "BOOLEAN") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1698181368829-3") {
        addColumn(tableName: "assessment_step_data") {
            column(name: "assessment_step_response_ref", type: "bigint") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1698181368829-4") {
        addForeignKeyConstraint(baseColumnNames: "assessment_step_response_ref", baseTableName: "assessment_step_data", constraintName: "FKfiv2or8rcm1jjhdpnkv27a214", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_step_response", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1698181368829-5") {
        dropColumn(columnName: "result", tableName: "assessment_step_data")
    }
}
