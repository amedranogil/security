/*******************************************************************************
 * Copyright 2016 Universidad Politécnica de Madrid UPM
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.universAAL.security.authorisator;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.universAAL.ioc.dependencies.impl.PassiveDependencyProxy;
import org.universAAL.middleware.container.ModuleContext;
import org.universAAL.middleware.container.utils.LogUtils;
import org.universAAL.middleware.owl.ManagedIndividual;
import org.universAAL.middleware.rdf.Resource;
import org.universAAL.middleware.serialization.MessageContentSerializer;
import org.universAAL.middleware.service.CallStatus;
import org.universAAL.middleware.service.ServiceCall;
import org.universAAL.middleware.service.ServiceCallee;
import org.universAAL.middleware.service.ServiceResponse;
import org.universAAL.middleware.service.owls.process.ProcessOutput;
import org.universAAL.middleware.service.owls.profile.ServiceProfile;
import org.universAAL.ontology.profile.User;
import org.universAAL.ontology.security.AccessRight;
import org.universAAL.ontology.security.AccessType;
import org.universAAL.ontology.security.Role;
import org.universAAL.ontology.security.SecuritySubprofile;
import org.universAAL.security.authorisator.access_checkers.AssetDefaultAccessChecker;
import org.universAAL.security.authorisator.access_checkers.CheckUserRoles;
import org.universAAL.security.authorisator.interfaces.AccessChecker;
import org.universAAL.security.authorisator.profiles.AuthorisationServiceProfile;

/**
 * @author amedrano
 *
 */
public class AuthorisatorCallee extends ServiceCallee {

	private static final String AUX_BAG_OBJECT = ProjectActivator.NAMESPACE + "auxilaryBagObject";
	private static final String AUX_BAG_PROP =  ProjectActivator.NAMESPACE + "auxilaryBagProperty";
	private static List<AccessChecker> checkers = new ArrayList<AccessChecker>();
	private PassiveDependencyProxy<MessageContentSerializer> serializer;
	private CHeQuerrier query;
	/**
	 * @param context
	 * @param realizedServices
	 */
	public AuthorisatorCallee(ModuleContext context,
			ServiceProfile[] realizedServices) {
		super(context, realizedServices);
		serializer = new PassiveDependencyProxy<MessageContentSerializer>(
				context,
				new Object[] { MessageContentSerializer.class.getName() });
		query = new CHeQuerrier(owner);
		registerChecker(new AssetDefaultAccessChecker());
		registerChecker(new CheckUserRoles());
	}

	/**{@inheritDoc} */
	@Override
	public void communicationChannelBroken() {

	}	
	/**{@inheritDoc} */
	@Override
	public ServiceResponse handleCall(ServiceCall call) {
		String callURI = call.getProcessURI();
		if (callURI.contains(ProjectActivator.ADD_ROLE_SP)){
			SecuritySubprofile ssp = (SecuritySubprofile) call.getInputValue(ProjectActivator.SUBPROFILE);
			Role r = (Role) call.getInputValue(ProjectActivator.ROLE);
			
			ssp.addrole(r);
			
			// update SSP role prop in CHe
			updateProperty(ssp,SecuritySubprofile.PROP_ROLES);
			return new ServiceResponse(CallStatus.succeeded);
		}
		if (callURI.contains(ProjectActivator.REMOVE_ROLE_SP)){
			SecuritySubprofile ssp = (SecuritySubprofile) call.getInputValue(ProjectActivator.SUBPROFILE);
			Role r = (Role) call.getInputValue(ProjectActivator.ROLE);
		
			List roles = ssp.getRoles();
			roles.remove(r);
			ssp.setProperty(SecuritySubprofile.PROP_ROLES, roles);
			
			// update SSP role prop in CHe
			updateProperty(ssp,SecuritySubprofile.PROP_ROLES);
			return new ServiceResponse(CallStatus.succeeded);
			
		}
		if (callURI.contains(ProjectActivator.ADD_ROLE_ROLE)){
			Role r = (Role) call.getInputValue(ProjectActivator.ROLE);
			Role sr = (Role) call.getInputValue(ProjectActivator.SUBROLE);
			
			r.addSubRole(sr);
			
			// update Role subroles in CHe
			updateProperty(r,Role.PROP_SUB_ROLES);
			return new ServiceResponse(CallStatus.succeeded);
		}
		if (callURI.contains(ProjectActivator.REMOVE_ROLE_ROLE)){
			Role r = (Role) call.getInputValue(ProjectActivator.ROLE);
			Role sr = (Role) call.getInputValue(ProjectActivator.SUBROLE);
			
			r.removeSubRole(sr);
			
			// update Role subroles in CHe
			updateProperty(r,Role.PROP_SUB_ROLES);
			return new ServiceResponse(CallStatus.succeeded);
			
		}
		if (callURI.contains(ProjectActivator.CHANGE_ROLE)){
			Role r = (Role) call.getInputValue(ProjectActivator.ROLE);
			
			// update AR in CHe
			updateObject(r);
			return new ServiceResponse(CallStatus.succeeded);
		}
		if (callURI.contains(ProjectActivator.GET_ROLES)){
			Object ret = getAllObjectsOfType(Role.MY_URI);
			ProcessOutput po = new ProcessOutput(ProjectActivator.ROLE, ret);
			ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
			sr.addOutput(po);
			return sr;
		}
		if (callURI.contains(ProjectActivator.ADD_AR_ROLE)){
			Role r = (Role) call.getInputValue(ProjectActivator.ROLE);
			AccessRight ar = (AccessRight) call.getInputValue(ProjectActivator.ACCESS_RIGHT);
			
			r.addAccessRight(ar);
			
			// update Role accessRights in CHe
			updateProperty(r,Role.PROP_HAS_ACCESS_RIGHTS);
			return new ServiceResponse(CallStatus.succeeded);
		}
		if (callURI.contains(ProjectActivator.REMOVE_AR_ROLE)){
			Role r = (Role) call.getInputValue(ProjectActivator.ROLE);
			AccessRight ar = (AccessRight) call.getInputValue(ProjectActivator.ACCESS_RIGHT);
			
			r.removeAccessRight(ar);
			
			// update Role accessRights in CHe
			updateProperty(r,Role.PROP_HAS_ACCESS_RIGHTS);
			return new ServiceResponse(CallStatus.succeeded);
		}
		if (callURI.contains(ProjectActivator.CHANGE_AR)){
			AccessRight ar = (AccessRight) call.getInputValue(ProjectActivator.ACCESS_RIGHT);
			
			// update AR in CHe
			updateObject(ar);
			return new ServiceResponse(CallStatus.succeeded);
		}
		if (callURI.contains(ProjectActivator.GET_AR)){
			Object ret = getAllObjectsOfType(AccessRight.MY_URI);
			ProcessOutput po = new ProcessOutput(ProjectActivator.ACCESS_RIGHT, ret);
			ServiceResponse sr = new ServiceResponse(CallStatus.succeeded);
			sr.addOutput(po);
			return sr;
		}
		if (callURI.contains("check")){
			
			Resource usr = (User) call.getInputValue(ProjectActivator.USER);
			if (usr == null){
				usr = call.getInvolvedUser(); 
			}
			if (usr == null || !ManagedIndividual.checkMembership(User.MY_URI, usr)){
				return new ServiceResponse(CallStatus.denied);
			}
			
			Resource asset = (Resource) call.getInputValue(ProjectActivator.ASSET);
			Set<AccessType> compiledAccess = new HashSet<AccessType>();
			for (AccessChecker ac : checkers) {
				compiledAccess.addAll(ac.checkAccess(owner,  (User) usr, asset));
			}
			
			AccessType requested = AuthorisationServiceProfile.getAccessType(callURI);
			LogUtils.logDebug(owner, getClass(), "handleCall", "Checking user AccesRight with type:" + requested.getURI());
			if (compiledAccess.contains(requested)){
				LogUtils.logDebug(owner, getClass(), "handleCall", "Access Granted!");
				return new ServiceResponse(CallStatus.succeeded);
			}else {
				LogUtils.logDebug(owner, getClass(), "handleCall", "Access Denied!");
				return new ServiceResponse(CallStatus.denied);
			}
		}

		return new ServiceResponse(CallStatus.noMatchingServiceFound);
	}

	public static Resource copy(Resource r, String newURI) {
		Resource copy = Resource.getResource(r.getType(), newURI);
		if (copy == null)
		    copy = new Resource(newURI);
		for (Enumeration e = r.getPropertyURIs(); e.hasMoreElements();) {
		    String key = (String) e.nextElement();
		    copy.setProperty(key, r.getProperty(key));
		}
		return copy;
	    }
	
	private boolean hasProperty(String rURI, String propURI){
		String out = query.unserialisedQuery(CHeQuerrier.getQuery(CHeQuerrier.getResource("hasProperty.sparql"), new String[]{rURI,propURI}));
		return Boolean.parseBoolean(out);
	}

	private void setProperty(Resource r, String prop) {
		
		Object propvalue = r.getProperty(prop);
		String propvalueURI = "";
		if (propvalue instanceof Resource) {
				if (!((Resource)propvalue).isAnon()) {
					propvalueURI = ((Resource) propvalue).getURI();
					propvalueURI = "<" + propvalueURI +"> .";
				}else {
					propvalueURI = "_:BN000000 .";
				}
		}
		
		
		String serialization = serializer.getObject().serialize(propvalue);
		
		String[] split = CHeQuerrier.splitPrefixes(serialization);
		
		String prefixes = split[0];
		String serialValue = split[1];
		query.unserialisedQuery(CHeQuerrier.getQuery(CHeQuerrier.getResource("setProperty.sparql"), new String[]{  prefixes,r.getURI(),prop, propvalueURI +  serialValue}));
		
	}
	
	private void updateProperty(Resource r, String prop) {
		
		if (!hasProperty(r.getURI(), prop)){
			setProperty(r, prop);
			return;
		}
		
		Object propvalue = r.getProperty(prop);
		String propvalueURI = "";
		if (propvalue instanceof Resource) {
			if (!((Resource)propvalue).isAnon()) {
				propvalueURI = ((Resource) propvalue).getURI();
				propvalueURI = "<" + propvalueURI +"> .";
			}else {
				propvalueURI = "_:BN000000 .";
			}
	}
		
		
		String serialization = serializer.getObject().serialize(propvalue);
		
		String[] split = CHeQuerrier.splitPrefixes(serialization);
		
		String prefixes = split[0];
		String serialValue = split[1];
		query.unserialisedQuery(CHeQuerrier.getQuery(CHeQuerrier.getResource("updateProperty.sparql"), new String[]{  prefixes,r.getURI(),prop, propvalueURI +  serialValue}));
		
	}

	private void updateObject(Resource r) {
		
		String serialization = serializer.getObject().serialize(r);
		
		String[] split = CHeQuerrier.splitPrefixes(serialization);
		
		String prefixes = split[0];
		String serialValue = split[1];
		query.unserialisedQuery(CHeQuerrier.getQuery(CHeQuerrier.getResource("updateFullObject.sparql"), new String[]{prefixes,r.getURI(), serialValue}));
		
	}

	private Object getAllObjectsOfType(String classuri){
		List val = new ArrayList();
		Object o = query.query( CHeQuerrier.getQuery(CHeQuerrier.getResource("getObjectType.sparql"), new String[]{AUX_BAG_OBJECT,AUX_BAG_PROP, classuri}));
		if (o instanceof Resource){
			o = ((Resource)o).getProperty(AUX_BAG_PROP);
			if (o instanceof List){
				List ol = (List) o;
				for (Object res : ol) {
					val.add(query.getFullResourceGraph(((Resource)o).getURI()));
				}
				return val;
			} else {
				return query.getFullResourceGraph(((Resource)o).getURI());
			}
		}else {
			LogUtils.logError(owner, getClass(), "getAllObjectsOfType", "Wrong querry response, should get a Resource and we didn't");
			return null;
		}
	}
	
	
	public static void registerChecker(AccessChecker ac){
		synchronized (checkers) {
			checkers.add(ac);
		}
	}
	public static void unregisterChecker(AccessChecker ac){
		synchronized (checkers) {
			checkers.remove(ac);
		}
	}
	public static void unregisterChecker(Class acc){
		for (AccessChecker ac : checkers) {
			if (ac.getClass().equals(acc)){
				unregisterChecker(ac);
			}
		}
	}
}
