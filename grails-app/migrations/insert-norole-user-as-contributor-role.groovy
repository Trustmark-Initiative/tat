
databaseChangeLog = {

    changeSet(author: 'rs239 (generated)', id: 'insert-norole-user-as-contributor-20119011-1')  {
        // Inser contributor role for all users with no roles.
        sql("""INSERT INTO user_role (role_id, user_id) SELECT 1, id FROM assessment_user where id NOT IN (SELECT urTemp.user_id FROM (SELECT user_id FROM user_role) urTemp);""")
    }
}
