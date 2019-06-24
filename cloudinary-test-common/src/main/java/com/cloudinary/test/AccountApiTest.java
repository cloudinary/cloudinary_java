package com.cloudinary.test;

import com.cloudinary.Account;
import com.cloudinary.api.ApiResponse;
import org.junit.*;
import org.junit.rules.TestName;

import java.util.*;

import static com.cloudinary.utils.ObjectUtils.emptyMap;
import static org.junit.Assert.*;

public class AccountApiTest extends MockableTest {

    // TODO this data should be fetched from env/gradle
    public static final String ACCOUNT_ID = "";
    public static final String API_KEY = "";
    public static final String API_SECRET = "";
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

    @Before
    public void setUp() throws Exception {
        System.out.println("Running " + this.getClass().getName() + "." + currentTest.getMethodName());
        this.account = new Account(ACCOUNT_ID, API_KEY, API_SECRET);
    }

    @AfterClass
    public static void tearDownClass() {
        Account account = new Account(ACCOUNT_ID, API_KEY, API_SECRET);
        for (String createdSubAccountId : createdSubAccountIds) {
            try {
                account.deleteSubAccount(createdSubAccountId, emptyMap());
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        }

        for (String userId : createdUserIds) {
            try {
                account.deleteUser(userId, null);
            } catch (Exception ignored) {
            }
        }

        for (String groupId : createdGroupIds) {
            try {
                account.deleteUserGroup(groupId, null);
            } catch (Exception ignored) {
            }
        }
    }

    // Sub accounts tests
    @Test
    public void testGetSubAccount() throws Exception {
        ApiResponse accountResponse = createSubAccount();
        ApiResponse account = this.account.getSubAccount(accountResponse.get("id").toString(), emptyMap());
        assertNotNull(account);
    }

    @Test
    public void testGetSubAccounts() throws Exception {
        createSubAccount();
        ApiResponse accounts = account.getSubAccounts(null, null, null, emptyMap());
        assertNotNull(accounts);
        assertTrue(((ArrayList) accounts.get("sub_accounts")).size() >= 1);
    }

    @Test
    public void testCreateSubAccount() throws Exception {
        ApiResponse result = createSubAccount();
        assertNotNull(result);
    }

    @Test
    public void testUpdateSubAccount() throws Exception {
        ApiResponse subAccount = createSubAccount();
        String newCloudName = randomLetters();
        ApiResponse result = account.updateSubAccount(subAccount.get("id").toString(), newCloudName, null, emptyMap(), null, null);
        assertNotNull(result);
        assertEquals(result.get("cloud_name"), newCloudName);
    }

    @Test
    public void testDeleteSubAccount() throws Exception {
        ApiResponse createResult = createSubAccount();
        ApiResponse result = account.deleteSubAccount(createResult.get("id").toString(), emptyMap());
        assertNotNull(result);
        assertEquals(result.get("message"), "ok");
    }

    // Users test
    @Test
    public void testGetUser() throws Exception {
        ApiResponse user = createUser();
        ApiResponse result = account.getUser(user.get("id").toString(), null);
        assertNotNull(result);
    }

    @Test
    public void testGetUsers() throws Exception {
        createUser();
        ApiResponse result = account.getUsers(null, null, null, null, null, null, null);
        assertNotNull(result);
        assertTrue(((ArrayList) result.get("users")).size() >= 1);
    }

    @Test
    public void testCreateUser() throws Exception {
        ApiResponse createResult = createSubAccount();
        ApiResponse result = createUser(Collections.singletonList(createResult.get("id").toString()));
        assertNotNull(result);
    }

    @Test
    public void testUpdateUser() throws Exception {
        ApiResponse user = createUser();

        String newName = randomLetters();
        ApiResponse result = account.updateUser(user.get("id").toString(), null, newName, null, null, null);
        assertNotNull(result);
        assertEquals(result.get("name"), newName);
    }

    @Test
    public void testDeleteUser() throws Exception {
        ApiResponse user = createUser(Collections.<String>emptyList());
        ApiResponse result = account.deleteUser(user.get("id").toString(), null);
        assertEquals(result.get("message"), "ok");

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
        ApiResponse result = account.deleteUserGroup(group.get("id").toString(), null);
        assertNotNull(result);
        assertEquals(result.get("ok"), true);
    }

    @Test
    public void testAddUserToUserGroup() throws Exception {
        ApiResponse user = createUser();
        ApiResponse group = createGroup();
        ApiResponse result = account.addUserToGroup(group.get("id").toString(), user.get("id").toString(), null);
        assertNotNull(result);
    }

    @Test
    public void testRemoveUserFromUserGroup() throws Exception {
        ApiResponse user = createUser();
        ApiResponse group = createGroup();
        String groupId = group.get("id").toString();
        String userId = user.get("id").toString();
        account.addUserToGroup(groupId, userId, null);
        ApiResponse result = account.removeUserFromGroup(groupId, userId, null);
        // TODO
        assertNotNull(result);
    }

    @Test
    public void testListUserGroups() throws Exception {
        createGroup();
        ApiResponse result = account.listUserGroups(null);
        assertNotNull(result);
        assertTrue(((List) result.get("user_groups")).size() >= 1);
    }

    @Test
    public void testListUserGroup() throws Exception {
        ApiResponse group = createGroup();
        ApiResponse result = account.getUserGroup(group.get("id").toString(), null);
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
        ApiResponse result = account.listUserGroupUsers(groupId, null);
        assertNotNull(result);
        assertTrue(((List) result.get("users")).size() >= 2);
    }


    // Helpers
    private ApiResponse createGroup() throws Exception {
        String name = randomLetters();
        createdGroupIds.add(name);
        return account.createUserGroup(name, null);

    }

    private ApiResponse createUser() throws Exception {
        return createUser(Collections.<String>emptyList());
    }

    private ApiResponse createUser(List<String> subAccountsIds) throws Exception {
        String email = String.format("%s@%s.com", randomLetters(), randomLetters());
        return account.createUser(email, "TestName", Account.Role.BILLING, subAccountsIds, null);
    }

    private ApiResponse createSubAccount() throws Exception {
        ApiResponse subAccount = account.createSubAccount(null, randomLetters(), emptyMap(), true, null, null);
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
