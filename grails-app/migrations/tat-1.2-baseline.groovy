databaseChangeLog = {

    changeSet(author: "rs239 (generated)", id: "1636063530707-1") {
        createTable(tableName: "assessment") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "assessmentPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime")

            column(name: "comment_last_change_user_ref", type: "BIGINT")

            column(name: "status_last_change_date", type: "datetime")

            column(name: "comment_last_change_date", type: "datetime")

            column(name: "organization_ref", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "status_last_change_user_ref", type: "BIGINT")

            column(name: "tds_and_tips_json", type: "CLOB")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "last_assessor_ref", type: "BIGINT")

            column(name: "assigned_to_ref", type: "BIGINT")

            column(name: "assessment_log_ref", type: "BIGINT")

            column(name: "result_comment", type: "CLOB")

            column(name: "created_by_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "contact_information_ref", type: "INT")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-2") {
        createTable(tableName: "assessment_artifact_data") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "assessment_artifact_dataPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "comment", type: "CLOB")

            column(name: "date_created", type: "datetime")

            column(name: "required_artifact_ref", type: "BIGINT")

            column(name: "modifying_user_ref", type: "BIGINT")

            column(name: "data_id", type: "BIGINT")

            column(name: "display_name", type: "VARCHAR(256)")

            column(name: "uploading_user_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-3") {
        createTable(tableName: "assessment_log") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "assessment_logPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-4") {
        createTable(tableName: "assessment_log_entry") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "assessment_log_entryPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "title", type: "VARCHAR(512)") {
                constraints(nullable: "false")
            }

            column(name: "message", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "assessment_log_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime")

            column(name: "json_val", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-5") {
        createTable(tableName: "assessment_step_data") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "assessment_step_dataPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "result_last_change_date", type: "datetime")

            column(name: "assessor_comment_user_id", type: "BIGINT")

            column(name: "last_result_user_id", type: "BIGINT")

            column(name: "last_comment_date", type: "datetime")

            column(name: "last_updated", type: "datetime")

            column(name: "EVIDENCE_INDICATES_NON_CONFORMANCE", type: "BOOLEAN")

            column(name: "result", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "last_checkbox_user_id", type: "BIGINT")

            column(name: "assessor_comment", type: "CLOB")

            column(name: "ORG_CLAIMS_NON_CONFORMANCE", type: "BOOLEAN")

            column(name: "assessment_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "ORG_CANNOT_PROVIDE_SUFFICIENT_EVIDENCE", type: "BOOLEAN")

            column(name: "td_assessment_step_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "EVIDENCE_INDICATES_PARTIAL_CONFORMANCE", type: "BOOLEAN")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-6") {
        createTable(tableName: "assessment_step_data_assessment_artifact_data") {
            column(name: "assessment_step_data_artifacts_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "artifact_data_id", type: "BIGINT")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-7") {
        createTable(tableName: "assessment_sub_step_data") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "assessment_sub_step_dataPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "result_last_change_date", type: "datetime")

            column(name: "assessor_comment_user_id", type: "BIGINT")

            column(name: "last_result_user_id", type: "BIGINT")

            column(name: "last_comment_date", type: "datetime")

            column(name: "last_updated", type: "datetime")

            column(name: "result", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "assessor_comment", type: "CLOB")

            column(name: "td_assessment_sub_step_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "assessment_step_data_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-8") {
        createTable(tableName: "assessment_td_link") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "assessment_td_linkPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "list_index", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "assessment_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "from_tip_ref", type: "BIGINT")

            column(name: "td_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-9") {
        createTable(tableName: "assessment_user") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "assessment_userPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "password_expired", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "contact_information_ref", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "organization_ref", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "account_expired", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "username", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "account_locked", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "pass_hash", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "enabled", type: "BOOLEAN") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-10") {
        createTable(tableName: "binary_data") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "binary_dataPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "file_system_path", type: "CLOB")

            column(name: "chunk_count", type: "INT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-11") {
        createTable(tableName: "binary_data_chunk") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "binary_data_chunkPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "byte_data", type: "MEDIUMBLOB") {
                constraints(nullable: "false")
            }

            column(name: "sequence_number", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "binary_data_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-12") {
        createTable(tableName: "binary_object") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "binary_objectPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "file_size", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime")

            column(name: "mime_type", type: "VARCHAR(128)") {
                constraints(nullable: "false")
            }

            column(name: "md5sum", type: "VARCHAR(512)")

            column(name: "original_filename", type: "VARCHAR(256)")

            column(name: "original_extension", type: "VARCHAR(32)")

            column(name: "created_by", type: "VARCHAR(128)")

            column(name: "content_id", type: "BIGINT")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-13") {
        createTable(tableName: "contact_grant") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "contact_grantPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime")

            column(name: "grant_id", type: "VARCHAR(64)") {
                constraints(nullable: "false")
            }

            column(name: "contact_information_id", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "organization_id", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "created_by_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-14") {
        createTable(tableName: "contact_information") {
            column(autoIncrement: "true", name: "id", type: "INT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "contact_informationPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "responder", type: "VARCHAR(512)")

            column(name: "mailing_address", type: "CLOB")

            column(name: "notes", type: "CLOB")

            column(name: "email", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "phone_number", type: "VARCHAR(32)")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-15") {
        createTable(tableName: "documents") {
            column(autoIncrement: "true", name: "id", type: "INT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "documentsPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "public_url", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "binary_object_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "public_document", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "url", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "organization_ref", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "document_category", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "file_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "CLOB")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-16") {
        createTable(tableName: "error_log") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "error_logPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "message", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime")

            column(name: "stack_trace", type: "CLOB")

            column(name: "context", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "error_message", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "cause_message", type: "CLOB")

            column(name: "cause_class", type: "CLOB")

            column(name: "error_class", type: "CLOB")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-17") {
        createTable(tableName: "mail_parameter") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "mail_parameterPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime")

            column(name: "field_name", type: "VARCHAR(254)") {
                constraints(nullable: "false")
            }

            column(name: "field_value", type: "CLOB")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-18") {
        createTable(tableName: "organization") {
            column(autoIncrement: "true", name: "id", type: "INT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "organizationPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "primary_contact_ref", type: "INT")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "uri", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "short_name", type: "VARCHAR(50)")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-19") {
        createTable(tableName: "organization_artifact") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "organization_artifactPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "date_created", type: "datetime")

            column(name: "organization_ref", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "active", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "binary_object_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "CLOB")

            column(name: "display_name", type: "VARCHAR(256)") {
                constraints(nullable: "false")
            }

            column(name: "uploading_user_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-20") {
        createTable(tableName: "organization_comment") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "organization_commentPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "title", type: "VARCHAR(256)") {
                constraints(nullable: "false")
            }

            column(name: "comment", type: "CLOB")

            column(name: "date_created", type: "datetime")

            column(name: "organization_id", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "user_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-21") {
        createTable(tableName: "organization_contact_information") {
            column(name: "organization_contacts_id", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "contact_information_id", type: "INT")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-22") {
        createTable(tableName: "parameter_value") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "parameter_valuePK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "step_data_ref", type: "BIGINT")

            column(name: "parameter_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "actual_value", type: "CLOB")

            column(name: "trustmark_ref", type: "BIGINT")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-23") {
        createTable(tableName: "role") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "rolePK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "authority", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-24") {
        createTable(tableName: "signing_certificates") {
            column(autoIncrement: "true", name: "id", type: "INT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "signing_certificatesPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "state_or_province_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "distinguished_name", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "revoking_user_id", type: "BIGINT")

            column(name: "date_created", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "organization_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "country_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "common_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "thumb_print", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "thumb_print_with_colons", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "organization_ref", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "revoked_timestamp", type: "datetime")

            column(name: "revoked_reason", type: "CLOB")

            column(name: "key_length", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "valid_period", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "x509_certificate", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "default_certificate", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "email_address", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "locality_name", type: "VARCHAR(255)")

            column(name: "certificate_public_url", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "organizational_unit_name", type: "VARCHAR(255)")

            column(name: "filename", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "private_key", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "expiration_date", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "serial_number", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-25") {
        createTable(tableName: "system_registry") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "system_registryPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "last_update", type: "date") {
                constraints(nullable: "false")
            }

            column(name: "registry_name", type: "VARCHAR(254)") {
                constraints(nullable: "false")
            }

            column(name: "registry_url", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-26") {
        createTable(tableName: "system_variable") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "system_variablePK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "field_value", type: "CLOB")

            column(name: "last_updated", type: "datetime")

            column(name: "field_name", type: "VARCHAR(254)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-27") {
        createTable(tableName: "td_assessment_step") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "td_assessment_stepPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "number", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(512)") {
                constraints(nullable: "false")
            }

            column(name: "trustmark_definition_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "identifier", type: "VARCHAR(512)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-28") {
        createTable(tableName: "td_assessment_step_artifact") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "td_assessment_step_artifactPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(256)") {
                constraints(nullable: "false")
            }

            column(name: "td_assessment_step_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "CLOB")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-29") {
        createTable(tableName: "td_assessment_step_td_criterion") {
            column(name: "assessment_step_criteria_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "conformance_criterion_id", type: "BIGINT")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-30") {
        createTable(tableName: "td_assessment_sub_step") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "td_assessment_sub_stepPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(512)") {
                constraints(nullable: "false")
            }

            column(name: "assessment_step_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-31") {
        createTable(tableName: "td_citation") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "td_citationPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "CLOB")

            column(name: "source", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-32") {
        createTable(tableName: "td_criterion") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "td_criterionPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(512)") {
                constraints(nullable: "false")
            }

            column(name: "number", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "td_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "CLOB")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-33") {
        createTable(tableName: "td_criterion_td_citation") {
            column(name: "conformance_criterion_citations_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "citation_id", type: "BIGINT")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-34") {
        createTable(tableName: "tip") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tipPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime")

            column(name: "base_uri", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "signature", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "tip_expression", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "source_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "tip_version", type: "VARCHAR(64)") {
                constraints(nullable: "false")
            }

            column(name: "out_of_date", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "publication_date_time", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "cached_url", type: "CLOB")

            column(name: "enabled", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "uri", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "CLOB")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-35") {
        createTable(tableName: "tip_reference") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tip_referencePK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "reference_name", type: "VARCHAR(512)") {
                constraints(nullable: "false")
            }

            column(name: "tip_ref", type: "BIGINT")

            column(name: "tip_sequence", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "owning_tip_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "notes", type: "CLOB")

            column(name: "td_ref", type: "BIGINT")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-36") {
        createTable(tableName: "tm_assessment_repo") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tm_assessment_repoPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "organization_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "repo_url", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-37") {
        createTable(tableName: "tm_assessment_repo_tm_trustmark") {
            column(name: "tmassessment_repository_trustmark_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "tmtrustmark_id", type: "BIGINT")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-38") {
        createTable(tableName: "tm_attribute") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tm_attributePK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "value", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-39") {
        createTable(tableName: "tm_binding_registry") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tm_binding_registryPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "url", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "organization_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "CLOB")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-40") {
        createTable(tableName: "tm_conformance_target_tips") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tm_conformance_target_tipsPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "require_compliance", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "conformance_target_tip_identifier", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "provider_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-41") {
        createTable(tableName: "tm_contact") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tm_contactPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "phone", type: "VARCHAR(255)")

            column(name: "first_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "organization_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR(255)")

            column(name: "last_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "email", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-42") {
        createTable(tableName: "tm_endpoint") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tm_endpointPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "published", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "url", type: "VARCHAR(255)")

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "type", type: "VARCHAR(255)")

            column(name: "provider_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "binding", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-43") {
        createTable(tableName: "tm_endpoint_tm_attribute") {
            column(name: "tmendpoint_attributes_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "tmattribute_id", type: "BIGINT")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-44") {
        createTable(tableName: "tm_group") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tm_groupPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-45") {
        createTable(tableName: "tm_organization") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tm_organizationPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "site_url", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "trustmark_provider", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "CLOB")

            column(name: "display_name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-46") {
        createTable(tableName: "tm_provider") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tm_providerPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "provider_type", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "organization_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "encrypt_cert", type: "CLOB")

            column(name: "entity_id", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "fully_compliant", type: "BOOLEAN")

            column(name: "sign_cert", type: "CLOB")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-47") {
        createTable(tableName: "tm_provider_name_formats") {
            column(name: "tmprovider_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "name_formats_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-48") {
        createTable(tableName: "tm_provider_protocols") {
            column(name: "tmprovider_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "protocols_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-49") {
        createTable(tableName: "tm_provider_tags") {
            column(name: "tmprovider_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "tags_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-50") {
        createTable(tableName: "tm_provider_tm_attribute") {
            column(name: "tmprovider_attributes_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "tmattribute_id", type: "BIGINT")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-51") {
        createTable(tableName: "tm_provider_tm_contact") {
            column(name: "tmprovider_contacts_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "tmcontact_id", type: "BIGINT")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-52") {
        createTable(tableName: "tm_registrant") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tm_registrantPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "organization_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "active", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "contact_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-53") {
        createTable(tableName: "tm_trustmark") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "tm_trustmarkPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "assessor_comments", type: "VARCHAR(255)")

            column(name: "url", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "provisional", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "provider_id", type: "BIGINT") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-54") {
        createTable(tableName: "trustmark") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "trustmarkPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "revoking_user_id", type: "BIGINT")

            column(name: "signing_certificate_id", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "recipient_contact_information_ref", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "assessor_comments", type: "CLOB")

            column(name: "status_url", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "has_exceptions", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "revoked_timestamp", type: "datetime")

            column(name: "revoked_reason", type: "CLOB")

            column(name: "provider_extension", type: "CLOB")

            column(name: "recipient_organization_ref", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "trustmark_definition_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "signed_json", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "expiration_date_time", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "generated_xml_ref", type: "BIGINT")

            column(name: "assessment_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "identifier_url", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "granting_user_ref", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "superseded_by_ref", type: "BIGINT")

            column(name: "definition_extension", type: "CLOB")

            column(name: "provider_organization_ref", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "provider_contact_information_ref", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "policy_publication_url", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "relying_party_agreement_url", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "signed_xml", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "status", type: "VARCHAR(255)") {
                constraints(nullable: "false")
            }

            column(name: "identifier", type: "VARCHAR(128)") {
                constraints(nullable: "false")
            }

            column(name: "issue_date_time", type: "datetime") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-55") {
        createTable(tableName: "trustmark_definition") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "trustmark_definitionPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "td_version", type: "VARCHAR(128)") {
                constraints(nullable: "false")
            }

            column(name: "last_updated", type: "datetime")

            column(name: "criteria_preface", type: "CLOB")

            column(name: "base_uri", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "signature", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "source_id", type: "BIGINT")

            column(name: "supersedes", type: "CLOB")

            column(name: "superseded_by", type: "CLOB")

            column(name: "name", type: "VARCHAR(256)") {
                constraints(nullable: "false")
            }

            column(name: "assessment_preface", type: "CLOB")

            column(name: "out_of_date", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "publication_date_time", type: "datetime") {
                constraints(nullable: "false")
            }

            column(name: "deprecated", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "cached_url", type: "CLOB")

            column(name: "uri", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "enabled", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "CLOB") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-56") {
        createTable(tableName: "trustmark_definition_parameter") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "trustmark_definition_parameterPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "kind", type: "VARCHAR(64)") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(128)") {
                constraints(nullable: "false")
            }

            column(name: "required", type: "BOOLEAN") {
                constraints(nullable: "false")
            }

            column(name: "assessment_step_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "CLOB")

            column(name: "identifier", type: "VARCHAR(128)") {
                constraints(nullable: "false")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-57") {
        createTable(tableName: "trustmark_definition_parameter_enum_values") {
            column(name: "td_parameter_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "enum_values_string", type: "VARCHAR(255)")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-58") {
        createTable(tableName: "trustmark_metadata") {
            column(autoIncrement: "true", name: "id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "trustmark_metadataPK")
            }

            column(name: "version", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "policy_url", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "time_period_no_exceptions", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "default_signing_certificate_id", type: "BIGINT") {
                constraints(nullable: "false")
            }

            column(name: "identifier_pattern", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "provider_id", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "relying_party_agreement_url", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "status_url_pattern", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "time_period_with_exceptions", type: "INT") {
                constraints(nullable: "false")
            }

            column(name: "name", type: "VARCHAR(128)") {
                constraints(nullable: "false")
            }

            column(name: "generator_class", type: "CLOB") {
                constraints(nullable: "false")
            }

            column(name: "description", type: "CLOB")
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-59") {
        createTable(tableName: "user_role") {
            column(name: "role_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "user_rolePK")
            }

            column(name: "user_id", type: "BIGINT") {
                constraints(nullable: "false", primaryKey: "true", primaryKeyName: "user_rolePK")
            }
        }
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-60") {
        addUniqueConstraint(columnNames: "username", constraintName: "UC_ASSESSMENT_USERUSERNAME_COL", tableName: "assessment_user")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-61") {
        addUniqueConstraint(columnNames: "grant_id", constraintName: "UC_CONTACT_GRANTGRANT_ID_COL", tableName: "contact_grant")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-62") {
        addUniqueConstraint(columnNames: "authority", constraintName: "UC_ROLEAUTHORITY_COL", tableName: "role")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-63") {
        addForeignKeyConstraint(baseColumnNames: "revoking_user_id", baseTableName: "trustmark", constraintName: "FK1w9clmr67m6wqf5odnm3opjio", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-64") {
        addForeignKeyConstraint(baseColumnNames: "assessment_step_ref", baseTableName: "td_assessment_sub_step", constraintName: "FK1xsaq20yeqarn4150cl66g1j8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "td_assessment_step", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-65") {
        addForeignKeyConstraint(baseColumnNames: "organization_ref", baseTableName: "signing_certificates", constraintName: "FK2dgym99x29ir9atlb37q1g3a6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-66") {
        addForeignKeyConstraint(baseColumnNames: "last_checkbox_user_id", baseTableName: "assessment_step_data", constraintName: "FK2exnj2pir44vo42sy7td1w5i7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-67") {
        addForeignKeyConstraint(baseColumnNames: "created_by_ref", baseTableName: "assessment", constraintName: "FK2fbk4fq304kw260rv7qwnxwmw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-68") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "tm_registrant", constraintName: "FK2laa97y1f29nwo4j2o93pa200", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-69") {
        addForeignKeyConstraint(baseColumnNames: "source_id", baseTableName: "trustmark_definition", constraintName: "FK382npveh3mcewiecj6ekdiql2", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "binary_object", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-70") {
        addForeignKeyConstraint(baseColumnNames: "contact_information_ref", baseTableName: "assessment_user", constraintName: "FK3bqjxu2rk6o05eor1ap09hse", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "contact_information", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-71") {
        addForeignKeyConstraint(baseColumnNames: "provider_id", baseTableName: "tm_endpoint", constraintName: "FK3gc9rpkmb6jo1iqv7xpp8q6lk", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_provider", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-72") {
        addForeignKeyConstraint(baseColumnNames: "trustmark_definition_ref", baseTableName: "td_assessment_step", constraintName: "FK3phxpod6ivy64uah48mcarui3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trustmark_definition", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-73") {
        addForeignKeyConstraint(baseColumnNames: "binary_object_ref", baseTableName: "documents", constraintName: "FK42xbhejhth1gbgixk6c1eiu3t", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "binary_object", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-74") {
        addForeignKeyConstraint(baseColumnNames: "tmattribute_id", baseTableName: "tm_provider_tm_attribute", constraintName: "FK47p0s4pg76ycclps3moktk898", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_attribute", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-75") {
        addForeignKeyConstraint(baseColumnNames: "uploading_user_ref", baseTableName: "organization_artifact", constraintName: "FK4f35kgdc5ejfi50jumnoknqcr", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-76") {
        addForeignKeyConstraint(baseColumnNames: "artifact_data_id", baseTableName: "assessment_step_data_assessment_artifact_data", constraintName: "FK4ranayob7ba6eoqsefhog2a64", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_artifact_data", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-77") {
        addForeignKeyConstraint(baseColumnNames: "contact_information_id", baseTableName: "contact_grant", constraintName: "FK5842b3ws0mwf788ddiua7qm9r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "contact_information", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-78") {
        addForeignKeyConstraint(baseColumnNames: "assessor_comment_user_id", baseTableName: "assessment_sub_step_data", constraintName: "FK5fof87aggktsxr2fg5lvkah5h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-79") {
        addForeignKeyConstraint(baseColumnNames: "assessment_log_ref", baseTableName: "assessment", constraintName: "FK5l3mkgjxs8rg1snwlj3ncrpiw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_log", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-80") {
        addForeignKeyConstraint(baseColumnNames: "step_data_ref", baseTableName: "parameter_value", constraintName: "FK5othkxhoamvpaxa83etg6pcw5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_step_data", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-81") {
        addForeignKeyConstraint(baseColumnNames: "recipient_organization_ref", baseTableName: "trustmark", constraintName: "FK5u3ke9cmxr8insbbyiayekwl5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-82") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "tm_provider", constraintName: "FK6m03f81g84mqe8unmwrso2ur4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-83") {
        addForeignKeyConstraint(baseColumnNames: "primary_contact_ref", baseTableName: "organization", constraintName: "FK6qiyuxcrevtnmqbvf2n3e7fir", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "contact_information", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-84") {
        addForeignKeyConstraint(baseColumnNames: "provider_id", baseTableName: "trustmark_metadata", constraintName: "FK7qnqxhs8wtfm7x5ctfbs8k7ww", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-85") {
        addForeignKeyConstraint(baseColumnNames: "tmprovider_id", baseTableName: "tm_provider_protocols", constraintName: "FK8lowci9h5273c1mwbkbns1xdj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_provider", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-86") {
        addForeignKeyConstraint(baseColumnNames: "assessment_step_id", baseTableName: "trustmark_definition_parameter", constraintName: "FK8ly0lotaa4orjspfnqf4v2db8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "td_assessment_step", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-87") {
        addForeignKeyConstraint(baseColumnNames: "assigned_to_ref", baseTableName: "assessment", constraintName: "FK95i3c19x9chn5w1vuhihsa0m3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-88") {
        addForeignKeyConstraint(baseColumnNames: "assessment_step_criteria_id", baseTableName: "td_assessment_step_td_criterion", constraintName: "FK9jyqy4xddeih0d53cng3sc6fb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "td_assessment_step", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-89") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "organization_comment", constraintName: "FKa0q1kbpki637crnnykpjgdf5r", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-90") {
        addForeignKeyConstraint(baseColumnNames: "role_id", baseTableName: "user_role", constraintName: "FKa68196081fvovjhkek5m97n3y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "role", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-91") {
        addForeignKeyConstraint(baseColumnNames: "generated_xml_ref", baseTableName: "trustmark", constraintName: "FKalsywsyi1tufi1c1firovkphm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "binary_object", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-92") {
        addForeignKeyConstraint(baseColumnNames: "td_parameter_id", baseTableName: "trustmark_definition_parameter_enum_values", constraintName: "FKaryjefw7n6d9de6joph3aakrg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trustmark_definition_parameter", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-93") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "contact_grant", constraintName: "FKb16x9cgqcc96vjtkyp1pu005y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-94") {
        addForeignKeyConstraint(baseColumnNames: "tmendpoint_attributes_id", baseTableName: "tm_endpoint_tm_attribute", constraintName: "FKbb099gokd0cn5072t4j58tvd4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_endpoint", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-95") {
        addForeignKeyConstraint(baseColumnNames: "binary_data_ref", baseTableName: "binary_data_chunk", constraintName: "FKbn1jc71b2ht8myiyus1y7mfx7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "binary_data", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-96") {
        addForeignKeyConstraint(baseColumnNames: "tmcontact_id", baseTableName: "tm_provider_tm_contact", constraintName: "FKbrqbrlxprhkw4gc891yqfy7k0", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_contact", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-97") {
        addForeignKeyConstraint(baseColumnNames: "tmprovider_contacts_id", baseTableName: "tm_provider_tm_contact", constraintName: "FKbta5s0n13p3ewlnc16es96aud", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_provider", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-98") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "tm_assessment_repo", constraintName: "FKcho7h85muvoxs9r6pyvjjbmkx", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-99") {
        addForeignKeyConstraint(baseColumnNames: "organization_ref", baseTableName: "documents", constraintName: "FKcpw4br4vkcqwy8f5ww2x1ugkv", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-100") {
        addForeignKeyConstraint(baseColumnNames: "tmtrustmark_id", baseTableName: "tm_assessment_repo_tm_trustmark", constraintName: "FKdjfu01f09594e5wgpsxpw4nyn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_trustmark", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-101") {
        addForeignKeyConstraint(baseColumnNames: "parameter_ref", baseTableName: "parameter_value", constraintName: "FKdr1e9mf7f8t6aade8oeh7n05v", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trustmark_definition_parameter", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-102") {
        addForeignKeyConstraint(baseColumnNames: "last_result_user_id", baseTableName: "assessment_step_data", constraintName: "FKe5pbdb3h8s7683f01ds4v1vvy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-103") {
        addForeignKeyConstraint(baseColumnNames: "last_assessor_ref", baseTableName: "assessment", constraintName: "FKe8p2ece5brcpkhkthsbisoicn", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-104") {
        addForeignKeyConstraint(baseColumnNames: "trustmark_definition_ref", baseTableName: "trustmark", constraintName: "FKece2ty3mt01slcke13gskiuoh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trustmark_definition", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-105") {
        addForeignKeyConstraint(baseColumnNames: "tmprovider_id", baseTableName: "tm_provider_name_formats", constraintName: "FKen80dmbmg6ejbkny4so91mpeh", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_provider", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-106") {
        addForeignKeyConstraint(baseColumnNames: "tmattribute_id", baseTableName: "tm_endpoint_tm_attribute", constraintName: "FKf2n7ecaagg1f5ebojjnh55ba3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_attribute", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-107") {
        addForeignKeyConstraint(baseColumnNames: "assessment_log_ref", baseTableName: "assessment_log_entry", constraintName: "FKfhf7se7vtkbhoo0aaasnqf45", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_log", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-108") {
        addForeignKeyConstraint(baseColumnNames: "source_id", baseTableName: "tip", constraintName: "FKfsa1i9ocnl4uhb6hfvhfa7bak", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "binary_object", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-109") {
        addForeignKeyConstraint(baseColumnNames: "assessment_ref", baseTableName: "trustmark", constraintName: "FKg9tqyblvkf2oukv85wa7w84ox", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-110") {
        addForeignKeyConstraint(baseColumnNames: "citation_id", baseTableName: "td_criterion_td_citation", constraintName: "FKgx13owlccau9od4flipvvu9ql", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "td_citation", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-111") {
        addForeignKeyConstraint(baseColumnNames: "content_id", baseTableName: "binary_object", constraintName: "FKgyfpgt7icaigynpbus0ooyb7y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "binary_data", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-112") {
        addForeignKeyConstraint(baseColumnNames: "user_ref", baseTableName: "organization_comment", constraintName: "FKh3uugdkr5twa0mwfmqbnx2men", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-113") {
        addForeignKeyConstraint(baseColumnNames: "td_assessment_step_ref", baseTableName: "td_assessment_step_artifact", constraintName: "FKhplugjk6mrip60fk1slulq27v", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "td_assessment_step", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-114") {
        addForeignKeyConstraint(baseColumnNames: "tmprovider_attributes_id", baseTableName: "tm_provider_tm_attribute", constraintName: "FKhqqpp9181eqgqe53prtqihjxy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_provider", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-115") {
        addForeignKeyConstraint(baseColumnNames: "organization_ref", baseTableName: "assessment", constraintName: "FKhrv333yiq2505n2scgw9au3xb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-116") {
        addForeignKeyConstraint(baseColumnNames: "data_id", baseTableName: "assessment_artifact_data", constraintName: "FKi2cw37vd1qa6n2qma3epodjox", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "binary_object", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-117") {
        addForeignKeyConstraint(baseColumnNames: "last_result_user_id", baseTableName: "assessment_sub_step_data", constraintName: "FKiey6q6xwxae1fp4gxy9obn2e6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-118") {
        addForeignKeyConstraint(baseColumnNames: "contact_information_ref", baseTableName: "assessment", constraintName: "FKiiattosr23etssbgv5l0d1glg", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "contact_information", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-119") {
        addForeignKeyConstraint(baseColumnNames: "conformance_criterion_citations_id", baseTableName: "td_criterion_td_citation", constraintName: "FKimgibbi93ggym0s6qrsh3n7mu", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "td_criterion", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-120") {
        addForeignKeyConstraint(baseColumnNames: "from_tip_ref", baseTableName: "assessment_td_link", constraintName: "FKiodfde79tcs0qqlkqsjdclsnf", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tip", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-121") {
        addForeignKeyConstraint(baseColumnNames: "recipient_contact_information_ref", baseTableName: "trustmark", constraintName: "FKiwluesc4hefebi8x0p9wln96k", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "contact_information", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-122") {
        addForeignKeyConstraint(baseColumnNames: "td_ref", baseTableName: "td_criterion", constraintName: "FKjsik5v3695id3bny63s5hogn5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trustmark_definition", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-123") {
        addForeignKeyConstraint(baseColumnNames: "comment_last_change_user_ref", baseTableName: "assessment", constraintName: "FKk3nm2401cy4xtqeyoseux5djo", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-124") {
        addForeignKeyConstraint(baseColumnNames: "assessment_step_data_ref", baseTableName: "assessment_sub_step_data", constraintName: "FKkd6dgce2bqkmng4jf3l6fh8tj", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_step_data", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-125") {
        addForeignKeyConstraint(baseColumnNames: "trustmark_ref", baseTableName: "parameter_value", constraintName: "FKkp09gaqsod44tdfuxig5746a4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trustmark", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-126") {
        addForeignKeyConstraint(baseColumnNames: "provider_id", baseTableName: "tm_trustmark", constraintName: "FKksp4iifllevjyvr0kmsb68srt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_provider", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-127") {
        addForeignKeyConstraint(baseColumnNames: "td_assessment_step_ref", baseTableName: "assessment_step_data", constraintName: "FKktk7lxmxas00ss4ccu4qygiw3", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "td_assessment_step", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-128") {
        addForeignKeyConstraint(baseColumnNames: "uploading_user_ref", baseTableName: "assessment_artifact_data", constraintName: "FKl0315467x0of7yrm0r72s01fp", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-129") {
        addForeignKeyConstraint(baseColumnNames: "td_ref", baseTableName: "tip_reference", constraintName: "FKl6xpo1lp5j8x2q0h80v5c0l5h", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trustmark_definition", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-130") {
        addForeignKeyConstraint(baseColumnNames: "tmprovider_id", baseTableName: "tm_provider_tags", constraintName: "FKll4dx9q5m784cf8ttkoraee04", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_provider", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-131") {
        addForeignKeyConstraint(baseColumnNames: "organization_ref", baseTableName: "organization_artifact", constraintName: "FKlt7slje0yw4foc9p5sqhea21d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-132") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "tm_binding_registry", constraintName: "FKlwl3kkuhounevi982kq7d8ehb", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-133") {
        addForeignKeyConstraint(baseColumnNames: "modifying_user_ref", baseTableName: "assessment_artifact_data", constraintName: "FKma8e2ylinytsw6cuoip9bu6gl", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-134") {
        addForeignKeyConstraint(baseColumnNames: "superseded_by_ref", baseTableName: "trustmark", constraintName: "FKmm46fuqg85gset52btqoopcww", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trustmark", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-135") {
        addForeignKeyConstraint(baseColumnNames: "tmassessment_repository_trustmark_id", baseTableName: "tm_assessment_repo_tm_trustmark", constraintName: "FKmsy8q852p0jdvtx6jjtuftqq5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_assessment_repo", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-136") {
        addForeignKeyConstraint(baseColumnNames: "assessment_step_data_artifacts_id", baseTableName: "assessment_step_data_assessment_artifact_data", constraintName: "FKn37vnnl8v3bm9ihpwb6xby2jt", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_step_data", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-137") {
        addForeignKeyConstraint(baseColumnNames: "contact_information_id", baseTableName: "organization_contact_information", constraintName: "FKn40u63fl3qnrfcy23825208r7", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "contact_information", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-138") {
        addForeignKeyConstraint(baseColumnNames: "tip_ref", baseTableName: "tip_reference", constraintName: "FKn4sufs31whs7wuw7udxsieig6", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tip", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-139") {
        addForeignKeyConstraint(baseColumnNames: "organization_id", baseTableName: "tm_contact", constraintName: "FKn7ttrytp8ro3r45b3q437yt0i", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-140") {
        addForeignKeyConstraint(baseColumnNames: "granting_user_ref", baseTableName: "trustmark", constraintName: "FKnbxsm28xuvqnia13386kxjluy", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-141") {
        addForeignKeyConstraint(baseColumnNames: "conformance_criterion_id", baseTableName: "td_assessment_step_td_criterion", constraintName: "FKno3oxlu2rulhce02lsmlk89oc", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "td_criterion", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-142") {
        addForeignKeyConstraint(baseColumnNames: "status_last_change_user_ref", baseTableName: "assessment", constraintName: "FKo4pm6fs3m5a40de81jd6wyuha", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-143") {
        addForeignKeyConstraint(baseColumnNames: "required_artifact_ref", baseTableName: "assessment_artifact_data", constraintName: "FKom305h8v98hkpyxo3pv3s1pof", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "td_assessment_step_artifact", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-144") {
        addForeignKeyConstraint(baseColumnNames: "assessment_ref", baseTableName: "assessment_step_data", constraintName: "FKouw156ehmg21k5qx0yidcx4ae", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-145") {
        addForeignKeyConstraint(baseColumnNames: "contact_ref", baseTableName: "tm_registrant", constraintName: "FKoww3i123q29mj0xx40uvvracm", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_contact", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-146") {
        addForeignKeyConstraint(baseColumnNames: "provider_id", baseTableName: "tm_conformance_target_tips", constraintName: "FKoxtrima47h9sg1srlhdrfao6y", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tm_provider", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-147") {
        addForeignKeyConstraint(baseColumnNames: "organization_contacts_id", baseTableName: "organization_contact_information", constraintName: "FKpifddsqqdoj2qsa5pta1o9769", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-148") {
        addForeignKeyConstraint(baseColumnNames: "user_id", baseTableName: "user_role", constraintName: "FKpr66ef6wt6ce0fex3neb743il", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-149") {
        addForeignKeyConstraint(baseColumnNames: "created_by_id", baseTableName: "contact_grant", constraintName: "FKpsqgylb82bnucf4dalvl736gi", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-150") {
        addForeignKeyConstraint(baseColumnNames: "organization_ref", baseTableName: "assessment_user", constraintName: "FKq7cmjkxpj2w1rmb1o38urgc9d", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-151") {
        addForeignKeyConstraint(baseColumnNames: "td_assessment_sub_step_ref", baseTableName: "assessment_sub_step_data", constraintName: "FKqesyl564j0l7kn7vvin2mda66", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "td_assessment_sub_step", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-152") {
        addForeignKeyConstraint(baseColumnNames: "td_ref", baseTableName: "assessment_td_link", constraintName: "FKrndw2vmdkoq6qrafdj8beek4x", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "trustmark_definition", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-153") {
        addForeignKeyConstraint(baseColumnNames: "assessor_comment_user_id", baseTableName: "assessment_step_data", constraintName: "FKrp1qx1tq4woeher8ryy7atbg4", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-154") {
        addForeignKeyConstraint(baseColumnNames: "binary_object_ref", baseTableName: "organization_artifact", constraintName: "FKs9yylnuims0c3k8ckccwbxnbw", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "binary_object", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-155") {
        addForeignKeyConstraint(baseColumnNames: "assessment_ref", baseTableName: "assessment_td_link", constraintName: "FKt23dwgafim9l3roi1ec4n2yew", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-156") {
        addForeignKeyConstraint(baseColumnNames: "provider_organization_ref", baseTableName: "trustmark", constraintName: "FKt33vgal8cbt7fop44c60dypk5", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "organization", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-157") {
        addForeignKeyConstraint(baseColumnNames: "revoking_user_id", baseTableName: "signing_certificates", constraintName: "FKtgga9n2fjscxbjhwu7lk5tjqd", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "assessment_user", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-158") {
        addForeignKeyConstraint(baseColumnNames: "owning_tip_ref", baseTableName: "tip_reference", constraintName: "FKthc50apb4jyq165j6k72mkhb8", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "tip", validate: "true")
    }

    changeSet(author: "rs239 (generated)", id: "1636063530707-159") {
        addForeignKeyConstraint(baseColumnNames: "provider_contact_information_ref", baseTableName: "trustmark", constraintName: "FKtl2gyquxlow2sy2ynjaasnk0j", deferrable: "false", initiallyDeferred: "false", referencedColumnNames: "id", referencedTableName: "contact_information", validate: "true")
    }
}
