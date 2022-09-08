databaseChangeLog = {
    include file: 'tat-1.2-baseline.groovy'
    include file: 'signingcertificate-remove-not-nullable-constraints.groovy'
    include file: 'organization-trustmark-provider.groovy'
    include file: 'add-trustmark-recipient-identifers-to-organizations.groovy'
    include file: 'add-trustmark-recipient-identifier-to-trustmarks.groovy'
    include file: 'normalize-user_roles-to-one-role-per-user.groovy'
    include file: 'insert-norole-user-as-contributor-role.groovy'
    include file: 'replace-document-categories-with-human-formatted-strings.groovy'
}
