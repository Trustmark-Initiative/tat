databaseChangeLog = {

    changeSet(author: "rs239 (generated)", id: "1673665215693-3") {
        addColumn(tableName: "assessment_user") {
            column(name: "contact_email", type: "varchar(1000)")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-4") {
        addColumn(tableName: "assessment_user") {
            column(name: "name_family", type: "varchar(1000)")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-5") {
        addColumn(tableName: "assessment_user") {
            column(name: "name_given", type: "varchar(1000)")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-6") {
        addColumn(tableName: "assessment_user") {
            column(name: "role_array_json", type: "varchar(1000)")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-7") {
        dropForeignKeyConstraint(baseTableName: "tm_registrant", constraintName: "FK2laa97y1f29nwo4j2o93pa200")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-8") {
        dropForeignKeyConstraint(baseTableName: "tm_endpoint", constraintName: "FK3gc9rpkmb6jo1iqv7xpp8q6lk")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-9") {
        dropForeignKeyConstraint(baseTableName: "tm_provider_tm_attribute", constraintName: "FK47p0s4pg76ycclps3moktk898")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-10") {
        dropForeignKeyConstraint(baseTableName: "tm_provider", constraintName: "FK6m03f81g84mqe8unmwrso2ur4")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-11") {
        dropForeignKeyConstraint(baseTableName: "tm_provider_protocols", constraintName: "FK8lowci9h5273c1mwbkbns1xdj")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-12") {
        dropForeignKeyConstraint(baseTableName: "user_role", constraintName: "FKa68196081fvovjhkek5m97n3y")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-13") {
        dropForeignKeyConstraint(baseTableName: "tm_endpoint_tm_attribute", constraintName: "FKbb099gokd0cn5072t4j58tvd4")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-14") {
        dropForeignKeyConstraint(baseTableName: "tm_provider_tm_contact", constraintName: "FKbrqbrlxprhkw4gc891yqfy7k0")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-15") {
        dropForeignKeyConstraint(baseTableName: "tm_provider_tm_contact", constraintName: "FKbta5s0n13p3ewlnc16es96aud")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-16") {
        dropForeignKeyConstraint(baseTableName: "tm_assessment_repo", constraintName: "FKcho7h85muvoxs9r6pyvjjbmkx")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-17") {
        dropForeignKeyConstraint(baseTableName: "tm_assessment_repo_tm_trustmark", constraintName: "FKdjfu01f09594e5wgpsxpw4nyn")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-18") {
        dropForeignKeyConstraint(baseTableName: "tm_provider_name_formats", constraintName: "FKen80dmbmg6ejbkny4so91mpeh")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-19") {
        dropForeignKeyConstraint(baseTableName: "tm_endpoint_tm_attribute", constraintName: "FKf2n7ecaagg1f5ebojjnh55ba3")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-20") {
        dropForeignKeyConstraint(baseTableName: "tm_provider_tm_attribute", constraintName: "FKhqqpp9181eqgqe53prtqihjxy")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-21") {
        dropForeignKeyConstraint(baseTableName: "tm_trustmark", constraintName: "FKksp4iifllevjyvr0kmsb68srt")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-22") {
        dropForeignKeyConstraint(baseTableName: "tm_provider_tags", constraintName: "FKll4dx9q5m784cf8ttkoraee04")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-23") {
        dropForeignKeyConstraint(baseTableName: "tm_binding_registry", constraintName: "FKlwl3kkuhounevi982kq7d8ehb")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-24") {
        dropForeignKeyConstraint(baseTableName: "tm_assessment_repo_tm_trustmark", constraintName: "FKmsy8q852p0jdvtx6jjtuftqq5")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-25") {
        dropForeignKeyConstraint(baseTableName: "tm_contact", constraintName: "FKn7ttrytp8ro3r45b3q437yt0i")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-26") {
        dropForeignKeyConstraint(baseTableName: "tm_registrant", constraintName: "FKoww3i123q29mj0xx40uvvracm")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-27") {
        dropForeignKeyConstraint(baseTableName: "tm_conformance_target_tips", constraintName: "FKoxtrima47h9sg1srlhdrfao6y")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-28") {
        dropForeignKeyConstraint(baseTableName: "user_role", constraintName: "FKpr66ef6wt6ce0fex3neb743il")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-29") {
        dropUniqueConstraint(constraintName: "UC_ROLEAUTHORITY_COL", tableName: "role")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-30") {
        dropTable(tableName: "role")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-31") {
        dropTable(tableName: "tm_assessment_repo")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-32") {
        dropTable(tableName: "tm_assessment_repo_tm_trustmark")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-33") {
        dropTable(tableName: "tm_attribute")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-34") {
        dropTable(tableName: "tm_binding_registry")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-35") {
        dropTable(tableName: "tm_conformance_target_tips")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-36") {
        dropTable(tableName: "tm_contact")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-37") {
        dropTable(tableName: "tm_endpoint")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-38") {
        dropTable(tableName: "tm_endpoint_tm_attribute")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-39") {
        dropTable(tableName: "tm_group")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-40") {
        dropTable(tableName: "tm_organization")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-41") {
        dropTable(tableName: "tm_provider")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-42") {
        dropTable(tableName: "tm_provider_name_formats")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-43") {
        dropTable(tableName: "tm_provider_protocols")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-44") {
        dropTable(tableName: "tm_provider_tags")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-45") {
        dropTable(tableName: "tm_provider_tm_attribute")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-46") {
        dropTable(tableName: "tm_provider_tm_contact")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-47") {
        dropTable(tableName: "tm_registrant")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-48") {
        dropTable(tableName: "tm_trustmark")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-49") {
        dropTable(tableName: "user_role")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-50") {
        dropColumn(columnName: "account_expired", tableName: "assessment_user")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-51") {
        dropColumn(columnName: "account_locked", tableName: "assessment_user")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-52") {
        dropColumn(columnName: "enabled", tableName: "assessment_user")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-53") {
        dropColumn(columnName: "pass_hash", tableName: "assessment_user")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-54") {
        dropColumn(columnName: "password_expired", tableName: "assessment_user")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-1") {
        dropNotNullConstraint(columnDataType: "int", columnName: "contact_information_ref", tableName: "assessment_user")
    }

    changeSet(author: "rs239 (generated)", id: "1673665215693-2") {
        dropNotNullConstraint(columnDataType: "int", columnName: "organization_ref", tableName: "assessment_user")
    }
}
