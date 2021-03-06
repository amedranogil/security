/*******************************************************************************
 * Copyright 2013 Ericsson Nikola Tesla d.d.
 *
 * See the NOTICE file distributed with this work for additional 
 * information regarding copyright ownership
 *	
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *	
 * http://www.apache.org/licenses/LICENSE-2.0
 *	
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.security.authorisator.utils;

import java.util.Iterator;
import java.util.List;

import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.DefaultServiceCaller;
import org.universAAL.middleware.service.ServiceCaller;
import org.universAAL.middleware.service.ServiceRequest;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.ontology.profile.Profilable;
import org.universAAL.ontology.profile.Profile;
import org.universAAL.ontology.profile.SubProfile;
import org.universAAL.ontology.profile.User;
import org.universAAL.ontology.profile.UserProfile;
import org.universAAL.ontology.profile.service.ProfilingService;
import org.universAAL.ontology.security.SecuritySubprofile;
import org.universAAL.ontology.security.UserPasswordCredentials;

/**
 * Communicates with Profiling Server and acts as a helper when initializing
 * {@link SecuritySubprofile} for a {@link User}
 * 
 * @author eandgrg
 * 
 */
public class ProfilingServerHelper {
    public static ModuleContext mc = null;
    public static final String NAMESPACE = "http://ontology.ent.hr/SecuritySubprofilePrerequisitesHelper#";

    public static final String OUTPUT_USERS = NAMESPACE + "OUT_USERS";
    public static final String OUTPUT_USER = NAMESPACE + "OUT_USER";
    public static final String OUTPUT_GETPROFILE = NAMESPACE
	    + "OUTPUT_GETPROFILE";
    private static final String OUTPUT_GETSUBPROFILES = NAMESPACE
	    + "outputSubprofile";
    public DefaultServiceCaller sc;

    public ProfilingServerHelper(ModuleContext mc) {
	ProfilingServerHelper.mc = mc;
	sc = new DefaultServiceCaller(mc);
    }

    /**
     * @param user
     *            {@link User}
     * @return if the {@link User} existed and was obtained from the Profiling
     *         Server return true, false otherwise
     */
    public boolean getUserSucceeded(Resource user) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(), null);
	req.addValueFilter(new String[] { ProfilingService.PROP_CONTROLS },
		user);
	req.addRequiredOutput(OUTPUT_USER,
		new String[] { ProfilingService.PROP_CONTROLS });

	ServiceResponse resp = sc.call(req);
	if (resp.getCallStatus() == CallStatus.succeeded) {
	    Object out = getReturnValue(resp.getOutputs(), OUTPUT_USER);
	    if (out != null) {
		LogUtils
			.logDebug(
				mc,
				this.getClass(),
				"getUserSucceeded",
				new Object[] { "User: "
					+ user.getURI()
					+ " obtained from Profiling server (so it exists)" },
				null);
		return true;
	    } else {
		return false;
	    }
	} else {
	    LogUtils.logDebug(mc, this.getClass(), "getUserSucceeded",
		    new Object[] { "Call for User: " + user.getURI()
			    + " not succeeded or User does not exist." }, null);
	    return false;
	}
    }

    /**
     * @param user
     *            {@link User}
     * @return
     */
    public String getUserAsString(Resource user) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(), null);
	req.addValueFilter(new String[] { ProfilingService.PROP_CONTROLS },
		user);
	req.addRequiredOutput(OUTPUT_USER,
		new String[] { ProfilingService.PROP_CONTROLS });

	ServiceResponse resp = sc.call(req);
	if (resp.getCallStatus() == CallStatus.succeeded) {
	    Object out = getReturnValue(resp.getOutputs(), OUTPUT_USER);
	    if (out != null) {
		return out.toString();
	    } else {

		return "nothing";
	    }
	} else {
	    return resp.getCallStatus().name();
	}
    }

    /**
     * 
     * @param user
     *            {@link User}
     * @return {@link User} obtained from Profiling server or null otherwise
     */
    public User getUser(Resource user) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(), null);
	req.addValueFilter(new String[] { ProfilingService.PROP_CONTROLS },
		user);
	req.addRequiredOutput(OUTPUT_USER,
		new String[] { ProfilingService.PROP_CONTROLS });

	ServiceResponse resp = sc.call(req);
	if (resp.getCallStatus() == CallStatus.succeeded) {

	    Object out = getReturnValue(resp.getOutputs(), OUTPUT_USERS);
	    if (out != null) {
		LogUtils.logDebug(mc, this.getClass(), "getUser", new Object[] {
			"User: ", user.getURI(),
			" retrieved from Profiling Server." }, null);
		return (User) out;
	    } else {
		LogUtils.logDebug(mc, this.getClass(), "getUser",
			new Object[] { "User not found in Profiling Server." },
			null);
		return null;
	    }
	} else {
	    LogUtils
		    .logDebug(
			    mc,
			    this.getClass(),
			    "getUser",
			    new Object[] { "Call for obtaining user did not succeed." },
			    null);
	    return null;
	}
    }

    /**
     * @param user
     *            {@link User}
     * @return if the {@link User} was successfully added in Profiling Server
     *         return true, false otherwise
     */
    public boolean addUserSucceeded(User user) {
	ServiceRequest sr = new ServiceRequest(new ProfilingService(), null);
	sr.addAddEffect(new String[] { ProfilingService.PROP_CONTROLS }, user);

	ServiceResponse res = sc.call(sr);
	if (res.getCallStatus() == CallStatus.succeeded) {
	    LogUtils.logDebug(mc, this.getClass(), "addUserSucceeded",
		    new Object[] { "New user: ", user.getURI(), " added." },
		    null);
	    return true;
	} else {
	    LogUtils.logDebug(mc, this.getClass(), "addUserSucceeded",
		    new Object[] { "Call for adding a user to Profiling server did not succeed." }, null);
	    return false;
	}
    }

    /**
     * @param user
     *            {@link User}
     * @return {@link UserProfile} for given {@link User} as String
     */
    public String getProfileForUserAsString(User user) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(), null);
	req.addValueFilter(new String[] { ProfilingService.PROP_CONTROLS },
		user);
	req.addRequiredOutput(OUTPUT_GETPROFILE, new String[] {
		ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE });

	ServiceResponse resp = sc.call(req);
	if (resp.getCallStatus() == CallStatus.succeeded) {
	    Object out = getReturnValue(resp.getOutputs(), OUTPUT_GETPROFILE);
	    if (out != null) {
		return out.toString();
	    } else {
		return "nothing";
	    }
	} else {
	    return resp.getCallStatus().name();
	}
    }

    /**
     * @param user
     *            {@link User}
     * @return {@link UserProfile} for given {@link User}
     */
    public UserProfile getProfileForUser(User user) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(), null);
	req.addValueFilter(new String[] { ProfilingService.PROP_CONTROLS },
		user);
	req.addRequiredOutput(OUTPUT_GETPROFILE, new String[] {
		ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE });

	ServiceResponse resp = sc.call(req);
	if (resp.getCallStatus() == CallStatus.succeeded) {
	    try {
		List userProfileList = resp.getOutput(OUTPUT_GETPROFILE, true);

		if (userProfileList == null || userProfileList.size() == 0) {
		    LogUtils
			    .logInfo(
				    mc,
				    this.getClass(),
				    "getProfileForUser",
				    new Object[] { "There are no user profiles for user: "
					    + user.getURI() }, null);
		    return null;
		}
		// just return 1st
		UserProfile up = (UserProfile) userProfileList.get(0);
		return up;

	    } catch (Exception e) {
		LogUtils.logError(mc, this.getClass(), "getProfileForUser",
			new Object[] { "got exception", e.getMessage() }, e);
		return null;
	    }
	} else {
	    LogUtils.logWarn(mc, this.getClass(), "getProfileForUser",
		    new Object[] { "callstatus is not succeeded" }, null);
	    return null;
	}
    }

    /**
     * @param user
     *            {@link User}
     * @return true if {@link UserProfile} for given {@link User} was obtained,
     *         false otherwise
     */
    public boolean getProfileForUserSucceeded(User user) {
	ServiceRequest req = new ServiceRequest(new ProfilingService(), null);
	req.addValueFilter(new String[] { ProfilingService.PROP_CONTROLS },
		user);
	req.addRequiredOutput(OUTPUT_GETPROFILE, new String[] {
		ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE });

	ServiceResponse resp = sc.call(req);
	if (resp.getCallStatus() == CallStatus.succeeded) {
	    Object out = getReturnValue(resp.getOutputs(), OUTPUT_GETPROFILE);
	    if (out != null) {
		LogUtils.logDebug(mc, this.getClass(),
			"getProfileForUserSucceeded",
			new Object[] { "UserProfile obtained for user "
				+ user.getURI() }, null);
		return true;
	    } else {
		return false;
	    }
	} else {
	    LogUtils
		    .logDebug(
			    mc,
			    this.getClass(),
			    "getProfileForUserSucceeded",
			    new Object[] { "Call for UserProfile for user: "
				    + user.getURI()
				    + " not succeeded or UserProfile for this user does not exist." },
			    null);
	    return false;
	}
    }

    /**
     * @param user
     *            {@link User}
     * @param userProfile
     *            {@link UserProfile}
     * @return true if {@link UserProfile} for given {@link User} was added to
     *         Profiling Server, false otherwise
     */
    public boolean addUserProfileToUser(User user, UserProfile userProfile) {
	ServiceRequest sr = new ServiceRequest(new ProfilingService(), null);
	sr
		.addValueFilter(
			new String[] { ProfilingService.PROP_CONTROLS }, user);
	sr.addAddEffect(new String[] { ProfilingService.PROP_CONTROLS,
		Profilable.PROP_HAS_PROFILE }, userProfile);

	ServiceResponse res = sc.call(sr);
	if (res.getCallStatus() == CallStatus.succeeded) {
	    LogUtils.logDebug(mc, this.getClass(), "addUserProfileToUser",
		    new Object[] {
			    "UserProfile: " + userProfile.getURI()
				    + " for user ", user.getURI(), " added." },
		    null);
	    return true;
	} else {
	    LogUtils.logDebug(mc, this.getClass(), "addUserProfileToUser",
		    new Object[] { "call status: not succeeded" }, null);
	    return false;
	}
    }

    /**
     * 
     * @param outputs
     * @param expectedOutput
     * @return value as an Object
     */
    public static final Object getReturnValue(List outputs,
	    String expectedOutput) {
	Object returnValue = null;
	if (!(outputs == null)) {
	    for (Iterator i = outputs.iterator(); i.hasNext();) {
		ProcessOutput output = (ProcessOutput) i.next();
		if (output.getURI().equals(expectedOutput))
		    if (returnValue == null)
			returnValue = output.getParameterValue();
	    }
	}
	return returnValue;
    }

    public User[] getUsers() {
	ServiceRequest sr = new ServiceRequest(new ProfilingService(), null);
	sr.addTypeFilter(new String[] { ProfilingService.PROP_CONTROLS },
		User.MY_URI);
	sr.addRequiredOutput(OUTPUT_USERS,
		new String[] { ProfilingService.PROP_CONTROLS });

	ServiceResponse res = sc.call(sr);
	if (res.getCallStatus() == CallStatus.succeeded) {
	    try {
		List userList = res.getOutput(OUTPUT_USERS, true);

		if (userList == null || userList.size() == 0) {
		    LogUtils.logInfo(mc, this.getClass(), "getUsers",
			    new Object[] { "there are no users" }, null);
		    return null;
		}

		User[] users = (User[]) userList.toArray(new User[userList
			.size()]);

		return users;

	    } catch (Exception e) {
		LogUtils.logError(mc, this.getClass(), "getUsers",
			new Object[] { "Got exception", e.getMessage() }, e);
		return null;
	    }
	} else {
	    LogUtils.logWarn(mc, this.getClass(), "getUsers",
		    new Object[] { "Callstatus is not succeeded" }, null);
	    return null;
	}
    }

    public static void logUsers(User[] users) {
	if (users == null)
	    return;

	String s = "\n------------ registered users: -----------\n";
	for (int i = 0; i < users.length; i++) {
	    s += "User #" + i + "\n";
	    s += users[i].toStringRecursive();
	}
	LogUtils.logDebug(mc, ProfilingServerHelper.class,
		"logUsers", new Object[] { s }, null);
    }
    
    /**
     * Adds given {@link SubProfile} to a Profiling Server and connects it with
     * given {@link User}.
     * 
     * @param profilable
     *            {@link User}
     * @param subProfile
     *            {@link SubProfile}
     * @return call status as a {@link String}
     */
    public String addSubprofileToUser(User profilable, SubProfile subProfile) {
	ServiceCaller caller = new DefaultServiceCaller(mc);
	ServiceRequest req = new ServiceRequest(new ProfilingService(), null);
	req.addValueFilter(new String[] { ProfilingService.PROP_CONTROLS },
		profilable);
	req.addAddEffect(new String[] { ProfilingService.PROP_CONTROLS,
		Profilable.PROP_HAS_PROFILE, Profile.PROP_HAS_SUB_PROFILE },
		subProfile);
	ServiceResponse resp = caller.call(req);

	LogUtils.logDebug(mc, this.getClass(), "addSubprofileToUser",
		new Object[] { "ui subprofile: " + subProfile.getURI()
			+ " created and connection attempt to user: "
			+ profilable.getURI() + " returned status: "
			+ resp.getCallStatus().name() }, null);

	return resp.getCallStatus().name();
    }

    /**
     * Updates given {@link SubProfile}.
     * 
     * @param subProfile
     *            {@link SubProfile}
     * @return call status as a {@link String}
     */
    public String changeSubProfile(SubProfile subProfile) {
	ServiceCaller caller = new DefaultServiceCaller(mc);
	ServiceRequest req = new ServiceRequest(new ProfilingService(), null);
	req.addChangeEffect(new String[] { ProfilingService.PROP_CONTROLS,
		Profilable.PROP_HAS_PROFILE, Profile.PROP_HAS_SUB_PROFILE },
		subProfile);
	ServiceResponse resp = caller.call(req);

	LogUtils.logDebug(mc, this.getClass(), "changeSubProfile",
		new Object[] { "ui subprofile: " + subProfile.getURI()
			+ " change reguest returned status: "
			+ resp.getCallStatus().name() }, null);

	return resp.getCallStatus().name();
    }

    /**
     * Retrieves {@link SecuritySubprofile} that belongs to a given
     * {@link User} from a Profiling Server (makes a service call). Note
     * {@link User} and {@link UserProfile} should exist and be connected with
     * {@link SecuritySubprofile} before obtainment
     * 
     * @param user
     *            {@link User}
     * 
     * @return {@link SecuritySubprofile} that belongs to a given
     *         {@link User} or null in all other cases.
     */
    public SecuritySubprofile getSecuritySubprofileForUser(User user) {
	ServiceCaller caller = new DefaultServiceCaller(mc);
	ServiceRequest req = new ServiceRequest(new ProfilingService(), null);
	req.addValueFilter(new String[] { ProfilingService.PROP_CONTROLS },
		user);
	// with this restriction no matching service found is returned!!
	// req.addTypeFilter(new String[] { ProfilingService.PROP_CONTROLS,
	// Profilable.PROP_HAS_PROFILE, Profile.PROP_HAS_SUB_PROFILE },
	// SecuritySubprofile.MY_URI);
	req.addRequiredOutput(OUTPUT_GETSUBPROFILES, new String[] {
		ProfilingService.PROP_CONTROLS, Profilable.PROP_HAS_PROFILE,
		Profile.PROP_HAS_SUB_PROFILE });
	ServiceResponse resp = caller.call(req);
	if (resp.getCallStatus() == CallStatus.succeeded) {
	    try {
		List<?> subProfiles = resp.getOutput(OUTPUT_GETSUBPROFILES,
			true);
		if (subProfiles == null || subProfiles.size() == 0) {
		    LogUtils
			    .logInfo(
				    mc,
				    this.getClass(),
				    "getSecuritySubprofilesForUser",
				    new Object[] { "there are no UIPreference sub profiles for user: "
					    + user.getURI() }, null);
		    // TODO create one if there is none (dm initializes default
		    // ones based on stereotype data for each user so this
		    // should not be necessary here)
		    return null;
		}

		Iterator<?> iter = subProfiles.iterator();
		while (iter.hasNext()) {
		    SubProfile subProfile = (SubProfile) iter.next();
		    if (subProfile.getClassURI().equals(
			    SecuritySubprofile.MY_URI)) {
			LogUtils
				.logInfo(
					mc,
					this.getClass(),
					"getSecuritySubprofilesForUser",
					new Object[] { "Following SecuritySubprofile obtained from Profiling server: "
						+ subProfile.getURI() }, null);
			// TODO what if there is more SecuritySubprofiles,
			// currently only 1st is returned then
			return (SecuritySubprofile) subProfile;
		    }
		}
		// TODO same as above comment
		return null;
	    } catch (Exception e) {
		LogUtils.logError(mc, this.getClass(), "getSecuritySubprofileForUser",
			new Object[] { "exception: " }, e);
		return null;
	    }
	} else {
	    LogUtils
		    .logWarn(mc, this.getClass(), "getSecuritySubprofileForUser",
			    new Object[] { "returned: "
				    + resp.getCallStatus().name() }, null);
	    return null;
	}
    }
    
    public UserPasswordCredentials getUserPasswordCredentialsForUser(User u){
	SecuritySubprofile ssp = getSecuritySubprofileForUser(u);
	List creds = ssp.getCredentials();
	for (Object c : creds) {
	    if (c instanceof UserPasswordCredentials)
		return (UserPasswordCredentials) c;
	}
	return null;
    }

    /**
     * Adds given {@link SubProfile} to a Profiling Server and connects it with
     * given {@link UserProfile}.
     * 
     * @param userProfile
     *            {@link UserProfile}
     * @param subProfile
     *            {@link SubProfile}
     * @return true if the operation succeeded or false otherwise
     */
    public boolean addSubprofileToUserProfile(UserProfile userProfile,
	    SubProfile subProfile) {
	ServiceCaller caller = new DefaultServiceCaller(mc);
	ServiceRequest req = new ServiceRequest(new ProfilingService(), null);
	req.addValueFilter(new String[] { ProfilingService.PROP_CONTROLS,
		Profilable.PROP_HAS_PROFILE }, userProfile);
	req.addAddEffect(new String[] { ProfilingService.PROP_CONTROLS,
		Profilable.PROP_HAS_PROFILE, Profile.PROP_HAS_SUB_PROFILE },
		subProfile);

	ServiceResponse resp = caller.call(req);
	if (resp.getCallStatus() == CallStatus.succeeded) {
	    LogUtils.logDebug(mc, this.getClass(),
		    "addSubprofileToUserProfile", new Object[] {
			    "SubProfile: " + subProfile.getURI()
				    + " added to UserProfile: ",
			    userProfile.getURI() }, null);
	    return true;
	} else {
	    LogUtils.logDebug(mc, this.getClass(),
		    "addSubprofileToUserProfile",
		    new Object[] { "callstatus : "
			    + resp.getCallStatus().name() }, null);
	    return false;
	}
    }
    
    public void addUser(User u, UserPasswordCredentials credendtials){
    	UserPasswordCredentials cred = new UserPasswordCredentials(u.getURI()+"UserPasswordCredentials");
    	cred.setUsername(credendtials.getUsername());
    	cred.setpassword(credendtials.getPassword());
    	cred.setDigestAlgorithm(credendtials.getDigestAlgorithm());
    	SecuritySubprofile sp = new SecuritySubprofile(u.getURI()+"SecuritySubprofile");
    	sp.changeProperty(SecuritySubprofile.PROP_CREDENTIALS, cred);
    	
    	if (addUserSucceeded(u)){
    		UserProfile up = new UserProfile(u.getURI()+"addedBySecurityTester");
    		if (addUserProfileToUser(u, up)){
    			addSubprofileToUser(u, sp);
    		}
    	}
    }
}
