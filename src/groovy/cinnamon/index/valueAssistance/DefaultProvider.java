package cinnamon.index.valueAssistance;

import cinnamon.index.ValueAssistanceProvider;

/**
 * The DefaultProvider simply allows any value.
 *
 */

public class DefaultProvider implements ValueAssistanceProvider {

	String params;
	
	public DefaultProvider(){
        
	}
	
	@Override
	public String getValueAssistance() {
		return "<valueAssistance><type>any</type><values/></valueAssistance>";
	}

	@Override
	public void setParams(String xml) {		
		// ignore any params.
	}

}
