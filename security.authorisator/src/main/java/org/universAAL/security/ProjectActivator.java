package org.universAAL.security;

import org.universAAL.middleware.container.ModuleActivator;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.owl.OntologyManagement;
import org.universAAL.middleware.owl.SimpleOntology;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.rdf.ResourceFactory;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ontology.profile.User;
import org.universAAL.ontology.profile.service.ProfilingService;
import org.universAAL.ontology.security.AccessRight;
import org.universAAL.ontology.security.Asset;
import org.universAAL.ontology.security.AuthorizationService;
import org.universAAL.ontology.security.Role;
import org.universAAL.ontology.security.RoleManagementService;
import org.universAAL.ontology.security.SecuritySubprofile;
import org.universAAL.security.profiles.AuthorisationServiceProfile;
import org.universAAL.security.profiles.RoleMngServiceProfile;
import org.universAAL.security.profiles.SecuritySubProfileRoleManagement;

public class ProjectActivator implements ModuleActivator {

	public static final String NAMESPACE = "http://security.universAAL.org/Authorisator#";
	
	static final String ADD_ROLE_SP = NAMESPACE + "addRoleToSubProfile";
	static final String REMOVE_ROLE_SP = NAMESPACE + "removeRoleFromSubProfile";
	static final String ADD_ROLE_ROLE =NAMESPACE + "addRoleToRole";
	static final String REMOVE_ROLE_ROLE = NAMESPACE + "removeRoleFromRole";
	static final String ADD_AR_ROLE = NAMESPACE + "addAccessRightToRole";
	static final String REMOVE_AR_ROLE = NAMESPACE + "removeAccessRightFromRole";
	static final String CHANGE_AR = NAMESPACE + "changeAccessRight";
	static final String CHECK_CHALLENGER_USER_READ = NAMESPACE + "checkReadAccess";
	static final String CHECK_CHALLENGER_USER_CHANGE = NAMESPACE + "checkChangeAccess";
	static final String CHECK_CHALLENGER_USER_ADD = NAMESPACE + "checkAddAccess";
	static final String CHECK_CHALLENGER_USER_REMOVE = NAMESPACE + "checkRemoveAccess";
	static final String CHANGE_ROLE = NAMESPACE + "changeRole";
	static final String GET_ROLES = NAMESPACE + "getAllRoles";
	static final String GET_AR = NAMESPACE + "getAllAccessRights";
	
	static final String SUBPROFILE = NAMESPACE + "securitysubprofile";
	static final String ROLE = NAMESPACE +"role";
	static final String SUBROLE = NAMESPACE +"subrole";
	static final String ACCESS_RIGHT = NAMESPACE + "accessright";
	static final String USER = NAMESPACE +"user";
	static final String ASSET = NAMESPACE +"asset";

	
	public static ModuleContext context;
	ServiceProfile[] profs = new ServiceProfile[15];

	private AuthorisatorCallee callee;
	
	public void start(ModuleContext ctxt) throws Exception {	
		context = ctxt;

		
		LogUtils.logDebug(context, getClass(), "start", "Starting.");
		/*
		 * uAAL stuff
		 */
		//definining profiles
		
		OntologyManagement.getInstance().register(context, 
				new SimpleOntology(RoleMngServiceProfile.MY_URI, RoleManagementService.MY_URI, new ResourceFactory() {
			    
			    public Resource createInstance(String classURI, String instanceURI,
				    int factoryIndex) {
				return new RoleMngServiceProfile(instanceURI);
			    }
			}));
		
		OntologyManagement.getInstance().register(context, 
				new SimpleOntology(AuthorisationServiceProfile.MY_URI, AuthorizationService.MY_URI, new ResourceFactory() {
			    
			    public Resource createInstance(String classURI, String instanceURI,
				    int factoryIndex) {
				return new AuthorisationServiceProfile(instanceURI);
			    }
			}));
		
		OntologyManagement.getInstance().register(context, 
				new SimpleOntology(SecuritySubProfileRoleManagement.MY_URI, ProfilingService.MY_URI, new ResourceFactory() {
			    
			    public Resource createInstance(String classURI, String instanceURI,
				    int factoryIndex) {
				return new SecuritySubProfileRoleManagement(instanceURI);
			    }
			}));
		/* 
		 * Role Management
		 */
		//add role to SubProfile
		SecuritySubProfileRoleManagement addRoleSP = new SecuritySubProfileRoleManagement(ADD_ROLE_SP);
		addRoleSP.addFilteringInput(SUBPROFILE, SecuritySubprofile.MY_URI, 1, 1, SecuritySubProfileRoleManagement.pp_subprofile);
		addRoleSP.addInputWithAddEffect(ROLE, Role.MY_URI, 1, 1, SecuritySubProfileRoleManagement.pp_roles);
		profs[0] = addRoleSP.getProfile();
		
		//remove role from SubProfile
		SecuritySubProfileRoleManagement remRoleSP = new SecuritySubProfileRoleManagement(REMOVE_ROLE_SP);
		addRoleSP.addFilteringInput(SUBPROFILE, SecuritySubprofile.MY_URI, 1, 1, SecuritySubProfileRoleManagement.pp_subprofile);
		addRoleSP.addInputWithRemoveEffect(ROLE, Role.MY_URI, 1, 1, SecuritySubProfileRoleManagement.pp_roles);
		profs[1] = remRoleSP.getProfile();
		
		//add role as subrole
		RoleMngServiceProfile addRole = new RoleMngServiceProfile(ADD_ROLE_ROLE);
		addRole.addFilteringInput(ROLE, Role.MY_URI, 1, 1, new String[]{RoleManagementService.PROP_ROLE});
		addRole.addInputWithAddEffect(SUBROLE, Role.MY_URI, 1, 1, new String[]{RoleManagementService.PROP_ROLE,Role.PROP_SUB_ROLES});
		profs[2] = addRole.getProfile();
		
		//remove role as subrole
		RoleMngServiceProfile remRole = new RoleMngServiceProfile(REMOVE_ROLE_ROLE);
		addRole.addFilteringInput(ROLE, Role.MY_URI, 1, 1, new String[]{RoleManagementService.PROP_ROLE});
		addRole.addInputWithRemoveEffect(SUBROLE, Role.MY_URI, 1, 1, new String[]{RoleManagementService.PROP_ROLE,Role.PROP_SUB_ROLES});
		profs[3] = remRole.getProfile();
		
		//change Role
		RoleMngServiceProfile changeR = new RoleMngServiceProfile(CHANGE_ROLE);
		changeR.addInputWithChangeEffect(ROLE, Role.MY_URI, 1, 1, new String[]{RoleManagementService.PROP_ROLE});
		profs[12] = changeR.getProfile();
		
		//get all Roles
		RoleMngServiceProfile getR = new RoleMngServiceProfile(GET_ROLES);
		getR.addOutput(ROLE, Role.MY_URI, 0, -1, new String [] {RoleManagementService.PROP_ROLE});
		profs[13] = getR.getProfile();
		
		//add AccessRight to role
		RoleMngServiceProfile addAR = new RoleMngServiceProfile(ADD_AR_ROLE);
		addRole.addFilteringInput(ROLE, Role.MY_URI, 1, 1, new String[]{RoleManagementService.PROP_ROLE});
		addRole.addInputWithAddEffect(ACCESS_RIGHT, AccessRight.MY_URI, 1, 1, new String[]{RoleManagementService.PROP_ROLE,Role.PROP_HAS_ACCESS_RIGHTS});
		profs[4] = addAR.getProfile();
		
		//remove AccessRight from role
		RoleMngServiceProfile remAR = new RoleMngServiceProfile(REMOVE_AR_ROLE);
		addRole.addFilteringInput(ROLE, Role.MY_URI, 1, 1, new String[]{RoleManagementService.PROP_ROLE});
		addRole.addInputWithRemoveEffect(ACCESS_RIGHT, AccessRight.MY_URI, 1, 1, new String[]{RoleManagementService.PROP_ROLE,Role.PROP_HAS_ACCESS_RIGHTS});
		profs[5] = remAR.getProfile();
		
		//change AccessRight
		RoleMngServiceProfile changeAR = new RoleMngServiceProfile(CHANGE_AR);
		addRole.addInputWithChangeEffect(ACCESS_RIGHT, AccessRight.MY_URI, 1, 1, new String[]{RoleManagementService.PROP_ROLE,Role.PROP_HAS_ACCESS_RIGHTS});
		profs[6] = changeAR.getProfile();
		
		//get all Access Rights
		RoleMngServiceProfile getAR = new RoleMngServiceProfile(GET_AR);
		getAR.addOutput(ACCESS_RIGHT, AccessRight.MY_URI, 0, -1, new String [] {RoleManagementService.PROP_ROLE,Role.PROP_HAS_ACCESS_RIGHTS});
		profs[14] = getAR.getProfile();
		
		/* 
		 * Access information
		 */
		//Check READ Access to asset by Challenger User
		AuthorisationServiceProfile readCheckCU = new AuthorisationServiceProfile(CHECK_CHALLENGER_USER_READ);
		readCheckCU.addFilteringInput(USER, User.MY_URI, 0, 1, new String[]{AuthorizationService.PROP_CHALLENGER_USER});
		readCheckCU.addFilteringInput(ASSET, Asset.MY_URI, 1, 1, new String[]{AuthorizationService.PROP_ASSET_ACCESS});
		profs[7] = readCheckCU.getProfile();
		
		//Check CHANGE Access to asset by Challenger User
		AuthorisationServiceProfile changeCheckCU = new AuthorisationServiceProfile(CHECK_CHALLENGER_USER_CHANGE);
		changeCheckCU.addFilteringInput(USER, User.MY_URI, 0, 1, new String[]{AuthorizationService.PROP_CHALLENGER_USER});
		changeCheckCU.addInputWithChangeEffect(ASSET, Asset.MY_URI, 1, 1, new String[]{AuthorizationService.PROP_ASSET_ACCESS});
		profs[8] = changeCheckCU.getProfile();
		
		//Check ADD Access to asset by Challenger User
		AuthorisationServiceProfile addCheckCU = new AuthorisationServiceProfile(CHECK_CHALLENGER_USER_ADD);
		addCheckCU.addFilteringInput(USER, User.MY_URI, 0, 1, new String[]{AuthorizationService.PROP_CHALLENGER_USER});
		addCheckCU.addInputWithAddEffect(ASSET, Asset.MY_URI, 1, 1, new String[]{AuthorizationService.PROP_ASSET_ACCESS});
		profs[9] = addCheckCU.getProfile();
		
		//Check REMOVE Access to asset by Challenger User
		AuthorisationServiceProfile remCheckCU = new AuthorisationServiceProfile(CHECK_CHALLENGER_USER_REMOVE);
		remCheckCU.addFilteringInput(USER, User.MY_URI, 0, 1, new String[]{AuthorizationService.PROP_CHALLENGER_USER});
		remCheckCU.addInputWithRemoveEffect(ASSET, Asset.MY_URI, 1, 1, new String[]{AuthorizationService.PROP_ASSET_ACCESS});
		profs[10] = remCheckCU.getProfile();
		
		callee = new AuthorisatorCallee(ctxt, profs);
		
		//TODO: create a context publisher which publishes user access to assets for accountability (combined with CHe)
		
		LogUtils.logDebug(context, getClass(), "start", "Started.");
	}


	public void stop(ModuleContext ctxt) throws Exception {
		LogUtils.logDebug(context, getClass(), "stop", "Stopping.");
		/*
		 * close uAAL stuff
		 */
		callee.close();
		
		LogUtils.logDebug(context, getClass(), "stop", "Stopped.");

	}

}