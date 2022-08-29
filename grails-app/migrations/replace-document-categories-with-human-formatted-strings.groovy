
databaseChangeLog = {

    changeSet(author: 'rs239 (generated)', id: 'replace-document-categories-20129016-1')  {
        sql("""UPDATE documents SET document_category = 
                CASE
                    WHEN document_category = 'TM_RECIPIENT_AGREEMENT' THEN 'Trustmark Recipient Agreement'
                    WHEN document_category = 'TM_RELYING_PARTY_AGREEMENT' THEN 'Trustmark Relying Party Agreement'
                    WHEN document_category = 'TM_POLICY' THEN 'Trustmark Policy'
                    WHEN document_category = 'TM_SIGNING_CERTIFICATE_POLICY' THEN 'Trustmark Signing Certificate Policy' 
                    ELSE document_category
                END;""")
    }
}
