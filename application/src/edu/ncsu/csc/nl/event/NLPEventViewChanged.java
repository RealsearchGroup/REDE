package edu.ncsu.csc.nl.event;

/**
 * Represents that the view should change to classifications or annotations
 * 
 * @author John Slankas
 */
public class NLPEventViewChanged extends NLPEvent  {

	private int _currentView;
	
	public NLPEventViewChanged(int currentView) {
		_currentView = currentView;
	}

	public int getCurrentView() {
		return _currentView;
	}
	
}
