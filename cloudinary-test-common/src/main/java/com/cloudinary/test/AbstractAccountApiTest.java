package com.cloudinary.test;


import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.provisioning.Account;
import com.cloudinary.utils.ObjectUtils;
import org.junit.*;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestName;

import java.util.*;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public abstract class AbstractAccountApiTest extends MockableTest {
    private static Random rand = new Random();
    protected Account account;
    private static Set<String> createdSubAccountIds = new HashSet<String>();
    private static Set<String> createdUserIds = new HashSet<String>();
    private static Set<String> createdGroupIds = new HashSet<String>();

    @BeforeClass
    public static void setUpClass() {

    }

    @Rule
    public TestName currentTest = new TestName();
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        this.account = new Account(new Cloudinary());
    }

    @AfterClass
    public static void tearDownClass() {
        System.out.println("Start TearDownClass");
        Account account = new Account(new Cloudinary());
        for (String createdSubAccountId : createdSubAccountIds) {
            try {
                account.deleteSubAccount(createdSubAccountId, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (String userId : createdUserIds) {
            try {
                account.deleteUser(userId, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        for (String groupId : createdGroupIds) {
            try {
                account.deleteUserGroup(groupId, null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("### Deleted - SubAccounts:"+createdSubAccountIds.size()+", Users:"+createdUserIds.size()+ ", UserGroups:"+createdGroupIds.size());
    }

    @Test
    public void testPassingCredentialsThroughOptions() throws Exception {
        int exceptions = 0;

        Map<String, Object> map = singletonMap("provisioning_api_secret", new Object()) ;
        try {
            this.account.subAccounts(true, null, null, map);
        } catch (IllegalArgumentException ignored){
            exceptions++;
        }

        map = singletonMap("provisioning_api_key", new Object()) ;
        try {
            this.account.subAccounts(true, null, null, map);
        } catch (IllegalArgumentException ignored){
            exceptions++;
        }

        map = new HashMap<String, Object>();
        map.put("provisioning_api_key", "abc");
        map.put("provisioning_api_secret", "def");

        try {
            this.account.subAccounts(true, null, null, map);
        } catch (Exception ex){
            assertTrue(ex.getMessage().contains("Invalid credentials"));
            exceptions++;
        }

        assertEquals(3, exceptions);
    }

    // Sub accounts tests
    @Test
    public void testGetSubAccount() throws Exception {
        ApiResponse accountResponse = createSubAccount();
        ApiResponse account = this.account.subAccount(accountResponse.get("id").toString(), null);
        assertNotNull(account);
    }

    @Test
    public void testGetSubAccounts() throws Exception {
        createSubAccount();
        ApiResponse accounts = account.subAccounts(null, null, null, null);
        assertNotNull(accounts);
        assertTrue(((ArrayList) accounts.get("sub_accounts")).size() >= 1);
    }

    @Test
    public void testCreateSubAccount() throws Exception {
        ApiResponse result = createSubAccount();
        assertNotNull(result);

        String message = "";
        try {
            // test that the parameters are passed correctly - throws exception since the from-account id doesn't exist:
            account.createSubAccount(randomLetters(), null, emptyMap(), true, "non-existing-id", null);
        } catch (Exception ex){
            message = ex.getMessage();
        }

        assertTrue(message.contains("cannot find sub account"));
    }

    @Test
    public void testUpdateSubAccount() throws Exception {
        ApiResponse subAccount = createSubAccount();
        String newCloudName = randomLetters();
        ApiResponse result = account.updateSubAccount(subAccount.get("id").toString(), null, newCloudName, Collections.<String, String>emptyMap(), null, null);
        assertNotNull(result);
        assertEquals(result.get("cloud_name"), newCloudName);
    }

    @Test
    public void testDeleteSubAccount() throws Exception {
        ApiResponse createResult = createSubAccount();
        String id = createResult.get("id").toString();
        ApiResponse result = account.deleteSubAccount(id, null);
        assertNotNull(result);
        assertEquals(result.get("message"), "ok");
        createdSubAccountIds.remove(id);
    }

    // Users test
    @Test
    public void testGetUser() throws Exception {
        ApiResponse user = createUser();
        String userId = user.get("id").toString();
        ApiResponse result = account.user(userId, null);

        assertNotNull(result);
        deleteUser(userId);
    }

    @Test
    public void testGetUsers() throws Exception {
        String user1Id = createUser(Account.Role.MASTER_ADMIN).get("id").toString();
        String user2Id = createUser(Account.Role.MASTER_ADMIN).get("id").toString();
        ApiResponse result = account.users(null, Arrays.asList(user1Id, user2Id), null, null, null);
        assertNotNull(result);
        final ArrayList users = (ArrayList) result.get("users");
        ArrayList<String> returnedIds = new ArrayList<String>(2);

        assertEquals("Should return two users", 2, users.size());

        returnedIds.add(((Map) users.get(0)).get("id").toString());
        returnedIds.add(((Map) users.get(1)).get("id").toString());

        assertTrue("User1 id should be in the result set", returnedIds.contains(user1Id));
        assertTrue("User2 id should be in the result set", returnedIds.contains(user2Id));
        deleteUser(user1Id);
        deleteUser(user2Id);
    }

    @Test
    public void testGetPendingUsers() throws Exception {
        String id = createUser(Account.Role.BILLING).get("id").toString();

        ApiResponse pending = account.users(true, Collections.singletonList(id), null, null, null);
        assertEquals(1, ((ArrayList) pending.get("users")).size());

        ApiResponse notPending = account.users(false, Collections.singletonList(id), null, null, null);
        assertEquals(0, ((ArrayList) notPending.get("users")).size());

        ApiResponse all = account.users(null, Collections.singletonList(id), null, null, null);
        assertEquals(1, ((ArrayList) all.get("users")).size());
    }

    @Test
    public void testGetUsersByPrefix() throws Exception {
        final long timeMillis = System.currentTimeMillis();
        final String userName = String.format("SDK TEST Get Users By Prefix %d", timeMillis);
        final String userEmail = String.format("sdk-test-get-users-by-prefix+%d@cloudinary.com", timeMillis);

        createUser(userName,
                userEmail,
                Account.Role.BILLING,
                Collections.<String>emptyList());

        ApiResponse userByPrefix = account.users(true, null, userName.substring(0, userName.length() - 1), null, null);
        assertEquals(1, ((ArrayList) userByPrefix.get("users")).size());

        ApiResponse userByNonExistingPrefix = account.users(true, null, userName + "zzz", null, null);
        assertEquals(0, ((ArrayList) userByNonExistingPrefix.get("users")).size());
    }

    @Test
    public void testGetUsersBySubAccountIds() throws Exception {
        ApiResponse subAccount = createSubAccount();
        final String subAccountId = subAccount.get("id").toString();

        final long timeMillis = System.currentTimeMillis();
        final String userName = String.format("SDK TEST Get Users By Sub Account Ids %d", timeMillis);
        final String userEmail = String.format("sdk-test-get-users-by-sub-account-ids+%d@cloudinary.com", timeMillis);

        createUser(userName,
                userEmail,
                Account.Role.BILLING,
                Collections.singletonList(subAccountId));

        ApiResponse usersBySubAccount = account.users(true, null, userName, subAccountId, null);
        assertEquals(1, ((ArrayList) usersBySubAccount.get("users")).size());
    }

    @Test
    public void testGetUserByLastLoginTrue() throws Exception {
        final long timeMillis = System.currentTimeMillis();
        final String userName = String.format("SDK TEST Get Users By Last Login True %d", timeMillis);
        final String userEmail = String.format("sdk-test-get-users-by-llt+%d@cloudinary.com", timeMillis);

        createUser(userName,
                userEmail,
                Account.Role.BILLING,
                Collections.<String>emptyList());

        ApiResponse userByLastLogin = account.users(true, null, userName.substring(0, userName.length() - 1), null, null, true, new Date(), new Date());
        assertEquals(0, ((ArrayList) userByLastLogin.get("users")).size());
    }

    @Test
    public void testGetUserByLastLoginFalse() throws Exception {
        final long timeMillis = System.currentTimeMillis();
        final String userName = String.format("SDK TEST Get Users By Last Login False %d", timeMillis);
        final String userEmail = String.format("sdk-test-get-users-by-llf+%d@cloudinary.com", timeMillis);

        createUser(userName,
                userEmail,
                Account.Role.BILLING,
                Collections.<String>emptyList());

        ApiResponse userByLastLogin = account.users(true, null, userName.substring(0, userName.length() - 1), null, null, false, new Date(), new Date());
        assertEquals(1, ((ArrayList) userByLastLogin.get("users")).size());
    }

    @Test
    public void testGetUsersThrowsWhenSubAccountIdDoesntExist() throws Exception {
        final String subAccountId = randomLetters();
        expectedException.expectMessage("Cannot find sub account with id " + subAccountId);
        account.users(true, null, null, subAccountId, null);
    }

    @Test
    public void testCreateUser() throws Exception {
        ApiResponse createResult = createSubAccount();
        ApiResponse result = createUser(Collections.singletonList(createResult.get("id").toString()));
        assertNotNull(result);
    }

    @Test
    public void testCreateUserWithOptions() throws Exception {
        ApiResponse createResult = createSubAccount();
        ApiResponse result = createUser(Collections.singletonList(createResult.get("id").toString()), ObjectUtils.emptyMap());
        assertNotNull(result);
    }

    @Test
    public void testCreateUserEnabled() throws Exception {
        ApiResponse createResult = createSubAccount();
        ApiResponse result = createUser(Collections.singletonList(createResult.get("id").toString()), true);
        assertTrue((Boolean) result.get("enabled"));
    }

    @Test
    public void testCreateUserDisabled() throws Exception {
        ApiResponse createResult = createSubAccount();
        ApiResponse result = createUser(Collections.singletonList(createResult.get("id").toString()), false);
        assertFalse((Boolean) result.get("enabled"));
    }

    @Test
    public void testUpdateUser() throws Exception {
        ApiResponse user = createUser(Account.Role.ADMIN);
        String userId = user.get("id").toString();
        String newName = randomLetters();
        ApiResponse result = account.updateUser(userId, newName, null, null, null, null);

        assertNotNull(result);
        assertEquals(result.get("name"), newName);
        deleteUser(userId);
    }

    @Test
    public void testUpdateUserEnabled() throws Exception {
        ApiResponse user = createUser(Account.Role.ADMIN);
        String userId = user.get("id").toString();
        String newName = randomLetters();
        ApiResponse result = account.updateUser(userId, newName, null, null, true, null, null);

        assertNotNull(result);
        assertTrue((Boolean) result.get("enabled"));
        deleteUser(userId);
    }

    @Test
    public void testUpdateUserDisabled() throws Exception {
        ApiResponse user = createUser(Account.Role.ADMIN);
        String userId = user.get("id").toString();
        String newName = randomLetters();
        ApiResponse result = account.updateUser(userId, newName, null, null, false, null, null);

        assertNotNull(result);
        assertFalse((Boolean) result.get("enabled"));
        deleteUser(userId);
    }

    @Test
    public void testDeleteUser() throws Exception {
        ApiResponse user = createUser(Collections.<String>emptyList());
        String id = user.get("id").toString();
        ApiResponse result = account.deleteUser(id, null);
        assertEquals(result.get("message"), "ok");
        createdUserIds.remove(id);
    }

    // groups
    @Test
    public void testCreateUserGroup() throws Exception {
        ApiResponse group = createGroup();
        assertNotNull(group);
    }

    @Test
    public void testUpdateUserGroup() throws Exception {
        ApiResponse group = createGroup();
        String newName = randomLetters();
        ApiResponse result = account.updateUserGroup(group.get("id").toString(), newName, null);
        assertNotNull(result);
    }

    @Test
    public void testDeleteUserGroup() throws Exception {
        ApiResponse group = createGroup();
        String id = group.get("id").toString();
        ApiResponse result = account.deleteUserGroup(id, null);
        assertNotNull(result);
        assertEquals(result.get("ok"), true);
        createdGroupIds.remove(id);
    }

    @Test
    public void testAddUserToUserGroup() throws Exception {
        ApiResponse user = createUser();
        ApiResponse group = createGroup();
        String userId = user.get("id").toString();
        ApiResponse result = account.addUserToGroup(group.get("id").toString(), userId, null);
        assertNotNull(result);
        deleteUser(userId);
    }

    @Test
    public void testRemoveUserFromUserGroup() throws Exception {
        ApiResponse user = createUser(Account.Role.MEDIA_LIBRARY_ADMIN);
        ApiResponse group = createGroup();
        String groupId = group.get("id").toString();
        String userId = user.get("id").toString();
        account.addUserToGroup(groupId, userId, null);
        ApiResponse result = account.removeUserFromGroup(groupId, userId, null);
        assertNotNull(result);
        deleteUser(userId);
    }

    @Test
    public void testListUserGroups() throws Exception {
        createGroup();
        ApiResponse result = account.userGroups();
        assertNotNull(result);
        assertTrue(((List) result.get("user_groups")).size() >= 1);
    }

    @Test
    public void testListUserGroup() throws Exception {
        ApiResponse group = createGroup();
        ApiResponse result = account.userGroup(group.get("id").toString(), null);
        assertNotNull(result);
    }

    @Test
    public void testListUsersInGroup() throws Exception {
        ApiResponse user1 = createUser();
        ApiResponse user2 = createUser();
        ApiResponse group = createGroup();
        String groupId = group.get("id").toString();
        String user1Id = user1.get("id").toString();
        String user2Id = user2.get("id").toString();
        account.addUserToGroup(groupId, user1Id, null);
        account.addUserToGroup(groupId, user2Id, null);
        ApiResponse result = account.userGroupUsers(groupId, null);
        assertNotNull(result);
        assertTrue(((List) result.get("users")).size() >= 2);
        deleteUser(user1Id);
        deleteUser(user2Id);
    }


    // Helpers
    private ApiResponse createGroup() throws Exception {
        String name = randomLetters();
        ApiResponse userGroup = account.createUserGroup(name);
        createdGroupIds.add(userGroup.get("id").toString());
        return userGroup;
    }

    private ApiResponse createUser() throws Exception {
        return createUser(Collections.<String>emptyList());
    }

    private ApiResponse createUser(Account.Role role) throws Exception {
        return createUser(Collections.<String>emptyList(), role);
    }

    private ApiResponse createUser(List<String> subAccountsIds) throws Exception {
        return createUser(subAccountsIds, Account.Role.BILLING);
    }

    private ApiResponse createUser(List<String> subAccountsIds, Map<String, Object> options) throws Exception {
        return createUser(subAccountsIds, Account.Role.BILLING, options);
    }

    private ApiResponse createUser(List<String> subAccountsIds, Boolean enabled) throws Exception {
        return createUser(subAccountsIds, Account.Role.BILLING, enabled);
    }

    private ApiResponse createUser(List<String> subAccountsIds, Account.Role role) throws Exception {
        String email = String.format("%s@%s.com", randomLetters(), randomLetters());
        return createUser("TestName", email, role, subAccountsIds);
    }

    private ApiResponse createUser(List<String> subAccountsIds, Account.Role role, Map<String, Object> options) throws Exception {
        String email = String.format("%s@%s.com", randomLetters(), randomLetters());
        ApiResponse user = account.createUser("TestUserJava"+new Date().toString(), email, role, null, subAccountsIds, options);
        createdUserIds.add(user.get("id").toString());
        return user;
    }

    private ApiResponse createUser(List<String> subAccountsIds, Account.Role role, Boolean enabled) throws Exception {
        String email = String.format("%s@%s.com", randomLetters(), randomLetters());
        ApiResponse user = account.createUser("TestUserJava"+new Date().toString(), email, role, enabled, subAccountsIds, null);
        createdUserIds.add(user.get("id").toString());
        return user;
    }

    private ApiResponse createUser(final String name, String email, Account.Role role, List<String> subAccountsIds) throws Exception {
        ApiResponse user = account.createUser(name, email, role, subAccountsIds, null);
        createdUserIds.add(user.get("id").toString());
        return user;
    }

    private void deleteUser(String userId){
        try {
            account.deleteUser(userId, null);
            createdUserIds.remove(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private ApiResponse createSubAccount() throws Exception {
        ApiResponse subAccount = account.createSubAccount(randomLetters(), null, emptyMap(), true, null);
        createdSubAccountIds.add(subAccount.get("id").toString());
        return subAccount;
    }

    private static String randomLetters() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append((char) ('a' + rand.nextInt('z' - 'a' + 1)));
        }
        return sb.toString();
    }
}
