package edu.ncsu.csc.nl.model.type;


/**
 * Represents a security object
 * 
 * 
 * @author John
 *
 */
public enum SecurityObjective {
	NONE("none","NA"),
	TECHNICAL("technical","T",NONE),
	OPERATIONAL("operational","O",NONE),
	MANAGEMENT("management","M", NONE),
	CONFIDENTIALITY("confidentiality","C",TECHNICAL),
	INTEGRITY("integrity","I",TECHNICAL),
	INTEGRITY_IMMUNITY("integrity: immunity","II", INTEGRITY),
	AVAILABILITY("availability","A",TECHNICAL),
	AVAILABILITY_SURVIVABILITY("availability:survivability", "AS", AVAILABILITY),
	AVAILABILITY_RES_UTILIZATION("availability:resource utilization", "AR", AVAILABILITY),
	ACCESS_CONTROL("access control", "AC", TECHNICAL),
	ACCESS_CONTROL_IDENTITY("ac: identification & authentication","IA", TECHNICAL),
	ACCESS_CONTROL_AUTHORIZATION("ac: authorization","AU",TECHNICAL),
	ACCOUNTABILITY("accountability","AY",TECHNICAL),
	NON_REPUDITION("non-repudiation","NR",TECHNICAL),
	AUDIT("audit","AD", TECHNICAL),
	AUDIT_INTRUSION("audit: intrusion detection", "IN", AUDIT),
	LOGGING("logging","LG",TECHNICAL),
	LOGGING_SECURITY("logging: security events","LS",LOGGING),
	LOGGING_INSFRASTRUCTURE("logging: infrastructure", "LI", LOGGING),
	PRIVACY("privacy","PR", TECHNICAL),
	PRIVACY_ANONYMITY("privacy:anonymity", "PA", PRIVACY),
	DATABASE("database","DB");
	
	private static final SecurityObjective[] _selectableList = { 
		CONFIDENTIALITY,INTEGRITY,INTEGRITY_IMMUNITY, AVAILABILITY, AVAILABILITY_SURVIVABILITY, AVAILABILITY_RES_UTILIZATION ,
		ACCESS_CONTROL, ACCESS_CONTROL_IDENTITY, ACCESS_CONTROL_AUTHORIZATION, ACCOUNTABILITY, NON_REPUDITION, AUDIT, AUDIT_INTRUSION, 
		LOGGING, LOGGING_SECURITY, LOGGING_INSFRASTRUCTURE, PRIVACY, PRIVACY_ANONYMITY, OPERATIONAL, MANAGEMENT, TECHNICAL, DATABASE}; 
	
	private static final SecurityObjective[] _selectableListNoDatabase = { 
		CONFIDENTIALITY,INTEGRITY,INTEGRITY_IMMUNITY, AVAILABILITY, AVAILABILITY_SURVIVABILITY, AVAILABILITY_RES_UTILIZATION ,
		ACCESS_CONTROL, ACCESS_CONTROL_IDENTITY, ACCESS_CONTROL_AUTHORIZATION, ACCOUNTABILITY, NON_REPUDITION, AUDIT, AUDIT_INTRUSION, 
		LOGGING, LOGGING_SECURITY, LOGGING_INSFRASTRUCTURE, PRIVACY, PRIVACY_ANONYMITY, OPERATIONAL, MANAGEMENT, TECHNICAL}; 	

	private static final SecurityObjective[] _internalListForClassifiers = { 
		CONFIDENTIALITY,INTEGRITY,INTEGRITY_IMMUNITY, AVAILABILITY, AVAILABILITY_SURVIVABILITY, ACCESS_CONTROL_IDENTITY, 
        ACCOUNTABILITY, NON_REPUDITION, PRIVACY, OPERATIONAL, MANAGEMENT, TECHNICAL}; 	
	
	private static final SecurityObjective[] _internalMinimalListForClassifiers = { 
		CONFIDENTIALITY,INTEGRITY, AVAILABILITY, ACCESS_CONTROL_IDENTITY,  ACCOUNTABILITY, PRIVACY}; 	
	
	private static java.util.ArrayList<SecurityObjective> _minimalListForClassifiers = new java.util.ArrayList<SecurityObjective>();
	
	private static java.util.ArrayList<SecurityObjective> _listForClassifiers = new java.util.ArrayList<SecurityObjective>();
	static {
		for (SecurityObjective so: _internalListForClassifiers) { _listForClassifiers.add(so); }
		
		for (SecurityObjective so: _internalMinimalListForClassifiers)  {_minimalListForClassifiers.add(so); }
	}
	
	private String _label;
	private String _abbreviation;
	private SecurityObjective _parentObjective;
	
	private SecurityObjective(String label, String abbreviation) {
		this(label,abbreviation,null);
	}
	
	private SecurityObjective(String label, String abbreviation,SecurityObjective parent) {
		_label = label;
		
		_abbreviation = abbreviation;
		_parentObjective = parent;
	}
	
	public SecurityObjective getParent() {
		return _parentObjective;
	}
	
	public String getAbbreviation() {
		return _abbreviation;
	}
	
	public String toString() {
		return _label;
	}
	
	public static SecurityObjective[] getSelectableList() {
		return _selectableList;
	}

	public static SecurityObjective[] getSelectableListWithoutDatabase() {
		return _selectableListNoDatabase;
	}
	
	public static java.util.List<SecurityObjective> getListForClassification() {
		return new java.util.ArrayList<SecurityObjective>(_listForClassifiers);
	}
	
	public static java.util.List<SecurityObjective> getMinimalListForClassification() {
		return _minimalListForClassifiers;
	}
}
