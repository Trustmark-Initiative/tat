
databaseChangeLog = {

    changeSet(author: 'rs239 (generated)', id: 'normalize-admin-roles-20119011-1')  {
        // All users that with an admin role will have all other roles reset
        // admin = 3, contributor = 2, report viewer = 1
        sql("""DELETE FROM user_role WHERE user_id IN (SELECT urTemp.user_id from (SELECT user_id FROM user_role WHERE role_id = 3) urTemp) and (role_id = 1 or role_id = 2);""")
    }

    changeSet(author: 'rs239 (generated)', id: 'normalize-contributor-roles-20119011-1')  {
        // All users with a contributor role will have all other roles reset
        sql("""DELETE FROM user_role WHERE user_id IN (SELECT urTemp.user_id from (SELECT user_id FROM user_role WHERE role_id = 2) urTemp) and (role_id = 1);""")
    }
}
