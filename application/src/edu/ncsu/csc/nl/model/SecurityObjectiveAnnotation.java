package edu.ncsu.csc.nl.model;

import java.io.Serializable;

import edu.ncsu.csc.nl.model.type.SecurityImpact;
import edu.ncsu.csc.nl.model.type.SecurityMitigation;
import edu.ncsu.csc.nl.model.type.SecurityObjective;

public class SecurityObjectiveAnnotation implements Comparable<SecurityObjectiveAnnotation>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private SecurityObjective  _securityObjective;
	private SecurityImpact     _securityImpact;
	private SecurityMitigation _securityMitigation;
	private boolean _implied;
	private String _indicativePhrase = "";
	
	public SecurityObjectiveAnnotation() {
	}

	public SecurityObjectiveAnnotation(SecurityObjective objective, SecurityImpact securityImpact,
			                           SecurityMitigation securityMitigation, boolean implied) {
		_securityObjective  = objective;
		_securityImpact     = securityImpact;
		_securityMitigation = securityMitigation;
		_implied            = implied;
	}
	
	public SecurityObjectiveAnnotation(SecurityObjectiveAnnotation soa) {
		_securityObjective  = soa.getSecurityObjective();
		_securityImpact     = soa.getSecurityImpact();
		_securityMitigation = soa.getSecurityMitigation();
		_implied            = soa.isImplied();
		_indicativePhrase   = soa.getIndicativePhrase();
}
	
	public SecurityObjective getSecurityObjective() {
		return _securityObjective;
	}
	
	public void setSecurityObjective(SecurityObjective objective) {
		_securityObjective = objective;
	}
	
	public SecurityImpact getSecurityImpact() {
		return _securityImpact;
	}
	public void setSecurityImpact(SecurityImpact securityImpact) {
		_securityImpact = securityImpact;
	}
	public SecurityMitigation getSecurityMitigation() {
		return _securityMitigation;
	}
	public void setSecurityMitigation(SecurityMitigation securityMitigation) {
		_securityMitigation = securityMitigation;
	}
	public boolean isImplied() {
		return _implied;
	}
	public void setImplied(boolean implied) {
		_implied = implied;
	}

	public String getIndicativePhrase() {
		return _indicativePhrase;
	}

	public void setIndicativePhrase(String phrase) {
		_indicativePhrase = phrase;
	}

	@Override
	public int compareTo(SecurityObjectiveAnnotation o) {
		if (o == null || o.getSecurityObjective() == null) { return -1; }
		if (this.getSecurityObjective() == null) { return -1; }
		return this.getSecurityObjective().getAbbreviation().compareTo(o.getSecurityObjective().getAbbreviation());
	}
}
