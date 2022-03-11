/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.collections.CollectionBean;

import de.ipb_halle.lbac.entity.InfoObject;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.service.InfoObjectService;
import de.ipb_halle.lbac.service.NodeService;
import java.io.File;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import org.apache.commons.lang.exception.ExceptionUtils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Singleton(name = "globalAdmissionContext")
@Startup
public class GlobalAdmissionContext implements Serializable {

    /**
     * This class provides (and initializes when necessary) some system
     * accounts, groups, ACLists etc.
     */
    private static final long serialVersionUID = 1L;
    public final static String PUBLIC_NODE_ID = "1e0f832b-3d9e-4ebb-9e68-5a9fc2d9bee8";
    protected String LBAC_PROPERTIES_PATH = "/install/conf/lbac_properties.xml";

    public final static Integer PUBLIC_GROUP_ID = 1;
    public final static Integer PUBLIC_ACCOUNT_ID = 2;
    public final static Integer OWNER_ACCOUNT_ID = 3;
    public final static Integer ADMIN_GROUP_ID = 4;
    public static final String NAME_OF_DEACTIVATED_USER = "deactivated";

    private CredentialHandler credentialHandler;

    @Inject
    private ACListService aclistService;

    @Inject
    private InfoObjectService infoObjectService;

    @Inject
    private MemberService memberService;

    @Inject
    private MembershipService membershipService;

    @Inject
    private NodeService nodeService;

    private ACList adminOnlyACL;
    private ACList noAccessACL;
    private ACList ownerAllPermACL;
    private static ACList publicReadACL;

    private Group adminGroup;
    private Group publicGroup;

    private Node publicNode;
    private User adminAccount;
    private User publicAccount;
    private User ownerAccount;

    private ConcurrentHashMap<String, LockoutInfo> intruderLockoutMap;
    private transient Logger logger;

    /**
     * default constructor
     */
    public GlobalAdmissionContext() {
        this.logger = LogManager.getLogger(this.getClass().getName());
        this.credentialHandler = new CredentialHandler();
        this.intruderLockoutMap = new ConcurrentHashMap<>();
    }

    /**
     * set up and load the various system accounts, groups, etc. into this
     * session for later use. Provides also application and database
     * initialization function.
     */
    @PostConstruct
    private void init() {
        try {
            /*
            this.logger.info("************ START REPAIR PERMCODES ***************");
            this.aclistService.repairPermCodes();
            this.logger.info("************ FINISH REPAIR PERMCODES **************");
             */
            this.publicNode = createPublicNode(UUID.fromString(PUBLIC_NODE_ID));
            this.publicGroup = createGroup(PUBLIC_GROUP_ID, AdmissionSubSystemType.BUILTIN, this.publicNode, "Public Group");
            createPublicAccount();
            this.ownerAccount = createOwnerAccount();
            this.adminGroup = createGroup(ADMIN_GROUP_ID, AdmissionSubSystemType.LOCAL, this.nodeService.getLocalNode(), "Admin Group");
            createAdminAccount();

            this.noAccessACL = this.aclistService.save(new ACList().setName("No Access ACL"));
            createAdminOnlyAcl();
            createPublicReadableAcl();
            createOwnerAllAcl();

            createInfoEntity(UserMgrBean.USERMGR_PERM_KEY, "don't care", this.adminOnlyACL);
            createInfoEntity("LDAP_ENABLE", "false", this.adminOnlyACL);
            createInfoEntity(CollectionBean.getCollectionsPermKey(), "don't care", this.adminOnlyACL);
        } catch (Exception e) {
            logger.error("Error at initialisiing {}", e);
        }

    }

    private void createAdminOnlyAcl() {
        this.adminOnlyACL = this.aclistService.save(new ACList()
                .setName("Admin Only ACL")
                .addACE(this.adminGroup,
                        new ACPermission[]{
                            ACPermission.permREAD,
                            ACPermission.permEDIT,
                            ACPermission.permCREATE,
                            ACPermission.permDELETE,
                            ACPermission.permCHOWN,
                            ACPermission.permGRANT,
                            ACPermission.permSUPER}));
    }

    private void createPublicReadableAcl() {
        this.publicReadACL = this.aclistService.save(
                new ACList()
                        .setName("Public Readable ACL")
                        .addACE(
                                this.adminGroup,
                                new ACPermission[]{
                                    ACPermission.permREAD,
                                    ACPermission.permEDIT,
                                    ACPermission.permCREATE,
                                    ACPermission.permDELETE,
                                    ACPermission.permCHOWN,
                                    ACPermission.permGRANT,
                                    ACPermission.permSUPER}
                        )
                        .addACE(
                                this.publicGroup,
                                new ACPermission[]{
                                    ACPermission.permREAD}
                        )
        );
    }

    private void createOwnerAllAcl() {
        this.ownerAllPermACL = this.aclistService.save(
                new ACList()
                        .setName("Owner all permissions ACL")
                        .addACE(
                                this.ownerAccount,
                                new ACPermission[]{
                                    ACPermission.permREAD,
                                    ACPermission.permEDIT,
                                    ACPermission.permCREATE,
                                    ACPermission.permDELETE,
                                    ACPermission.permCHOWN,
                                    ACPermission.permGRANT,
                                    ACPermission.permSUPER}));
    }

    /**
     * create the admin account
     */
    public void createAdminAccount() {
        try {
            User u = this.memberService.loadLocalAdminUser();
            if (u == null) {
                logger.warn("No admin account found!");
                u = new User();
                u.setLogin("admin");
                u.setName("Admin");
                u.setNode(this.nodeService.getLocalNode());
                Properties prop = new Properties();
                prop.loadFromXML(Files.newInputStream(Paths.get(LBAC_PROPERTIES_PATH), StandardOpenOption.READ));
                u.setPassword(this.credentialHandler.computeDigest(prop.getProperty("DEFAULT_ADMIN_PASSWORD")));
                u.setSubSystemType(AdmissionSubSystemType.LOCAL);
                u = this.memberService.save(u);

                logger.warn("Admin account successfully created");
            }
            this.membershipService.addMembership(u, u);
            this.membershipService.addMembership(this.adminGroup, u);
            this.membershipService.addMembership(this.publicGroup, u);
            this.adminAccount = u;
        } catch (Exception e) {
            logger.error("Unable to restore admin account");
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * create a group if it does not exist
     *
     * @param id the unique group id
     * @param type the type of the group (BUILTIN or LOCAL)
     * @param node the node of the group (usually nodeService.localNode or null)
     * @param name the name of the group
     * @return the group
     */
    private Group createGroup(Integer id, AdmissionSubSystemType type, Node node, String name) {
        Group g = this.memberService.loadGroupById(id);
        if (g == null) {
            g = new Group();
            g.setId(id);
            g.setName(name);
            g.setNode(node);
            g.setSubSystemType(type);
            g = this.memberService.save(g);
            this.membershipService.addMembership(g, g);
        }
        return g;
    }

    /**
     * create default InfoEntities if they do not yet exist. All default
     * InfoEntities are owned by the local administrator.
     *
     * @param key the key
     * @param val the default value
     * @param acl the access control list
     */
    private void createInfoEntity(String key, String val, ACList acl) {
        InfoObject ie = this.infoObjectService.loadByKey(key);
        if (ie == null) {
            this.infoObjectService.save((InfoObject) new InfoObject(key, val)
                    .setOwner(this.adminAccount)
                    .setACList(acl));
        }
    }

    /**
     * This user is used in ACEntries for the owner of an object It is important
     * to add the Membership!
     */
    public User createOwnerAccount() {
        User o = this.memberService.loadUserById(OWNER_ACCOUNT_ID);
        if (o == null) {
            o = new User();
            o.setId(OWNER_ACCOUNT_ID);
            o.setLogin("@owner");
            o.setName("Owner Account");
            o.setNode(this.publicNode);
            o.setPassword("*invalid*");
            o.setSubSystemType(AdmissionSubSystemType.BUILTIN);
            o = this.memberService.save(o);
        }
        this.membershipService.addMembership(o, o);
        return o;
    }

    /**
     * create the public account and assign necessary group memberships
     */
    private void createPublicAccount() {
        User p = this.memberService.loadUserById(PUBLIC_ACCOUNT_ID);
        if (p == null) {
            p = new User();
            p.setId(PUBLIC_ACCOUNT_ID);
            p.setLogin("@public");
            p.setName("Public Account");
            p.setNode(this.publicNode);
            p.setPassword("*invalid*");
            p.setSubSystemType(AdmissionSubSystemType.BUILTIN);
            p = this.memberService.save(p);
        }
        this.membershipService.addMembership(p, p);
        this.membershipService.addMembership(this.publicGroup, p);
        this.publicAccount = p;
    }

    private Node createPublicNode(UUID id) {
        Node n = this.nodeService.loadById(id);
        if (n == null) {
            n = new Node();
            n.setId(id);
            n.setBaseUrl("http://localhost/");
            n.setInstitution("Public / Anonymous node");
            n.setPublicNode(true);
            n.setLocal(false);
            n = this.nodeService.save(n);
        }
        return n;
    }

    public User getAdminAccount() {
        return this.adminAccount;
    }

    public ACList getAdminOnlyACL() {
        return this.adminOnlyACL;
    }

    public CredentialHandler getCredentialHandler() {
        return this.credentialHandler;
    }

    public ACList getNoAccessACL() {
        return this.noAccessACL;
    }

    public ACList getOwnerAllPermACL() {
        return ownerAllPermACL;
    }

    public User getPublicAccount() {
        return this.publicAccount;
    }

    public Group getPublicGroup() {
        return this.publicGroup;
    }

    public static ACList getPublicReadACL() {
        return GlobalAdmissionContext.publicReadACL;
    }

    /**
     * prevent password brute forcing
     *
     * @param login user name to check
     * @param ipAddress ip address of the user request
     * @return true if the account or the address is locked, false otherwise
     */
    public boolean intruderLockoutCheck(String login, String ipAddress) {
        boolean result = false;

        intruderLockoutExpire();
        if (ipAddress != null) {
            result |= intruderLockoutCheckRecord(ipAddress);
        }
        result |= intruderLockoutCheckRecord(login);
        return result;
    }

    /**
     * perform a check operation on a single record
     */
    private boolean intruderLockoutCheckRecord(String key) {
        LockoutInfo loi = this.intruderLockoutMap.get(key);
        if (loi == null) {
            loi = new LockoutInfo();
            this.intruderLockoutMap.put(key, loi);
        }
        return loi.check();
    }

    /**
     * remove all expired LockoutInfo records
     */
    private void intruderLockoutExpire() {
        long time = new Date().getTime();
        this.intruderLockoutMap.entrySet().removeIf(e -> e.getValue().expired(time));
    }

    /**
     * increment the lock record for a given user and ip address
     *
     * @param login the login of the user
     * @param ipAddress the ip address of the user request
     */
    public void intruderLockoutLock(String login, String ipAddress) {
        LockoutInfo loi;
        if (ipAddress != null) {
            this.intruderLockoutLockRecord(ipAddress);
            this.logger.info("intruderLockoutLock() locking IP address: " + ipAddress);
        }
        this.intruderLockoutLockRecord(login);
    }

    /**
     * perform a lock operation on a single record
     *
     * @param key the record key in the intruderLockoutMap
     */
    private void intruderLockoutLockRecord(String key) {
        LockoutInfo loi = this.intruderLockoutMap.get(key);
        if (loi == null) {
            loi = new LockoutInfo();
            this.intruderLockoutMap.put(key, loi);
        }
        loi.lock();
    }

    /**
     * reset the intruder lockout
     *
     * @param login the login name of the successfully logged in user
     * @param ipAddress the source ip address of the users request
     */
    public void intruderLockoutUnlock(String login, String ipAddress) {
        if (ipAddress != null) {
            intruderLockoutUnlockRecord(ipAddress);
        }
        intruderLockoutUnlockRecord(login);
    }

    /**
     * perform a unlock operation on a single record
     *
     * @param key the record key in the intruderLockoutMap
     */
    private void intruderLockoutUnlockRecord(String key) {
        LockoutInfo loi = this.intruderLockoutMap.get(key);
        if (loi != null) {
            loi.unlock();
        }
    }

    public void setLBAC_PROPERTIES_PATH(String LBAC_PROPERTIES_PATH) {
        this.LBAC_PROPERTIES_PATH = LBAC_PROPERTIES_PATH;
    }

}
