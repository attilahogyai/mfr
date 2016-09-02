package org.mfr.zul.comp;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlMacroComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;

public class CheckBox extends HtmlMacroComponent {
	private static final long serialVersionUID = 6574772249579565925L;
	private boolean selected;
	private String value;
	@Wire
	private Div cont;
	
	private static final String activeStyle="check-box-selected hand";
	private static final String inActiveStyle="check-box hand";

	public CheckBox(){
		compose();
	}
	public CheckBox(Component parent){
		this.setParent(parent);
		compose();
		cont.addEventListener("onClick", new EventListener<Event>() {
			public void onEvent(Event e){
				setSelected(!isSelected());
				Events.postEvent("onCheck", CheckBox.this, new Event("onCheck", CheckBox.this));
			}
		});
		setSelected(false);
	}
	public boolean isSelected() {
		return selected;
	}
	public void setSelected(boolean selected) {
		if(selected){
			cont.setSclass(activeStyle);
		}else{
			cont.setSclass(inActiveStyle);
		}
		this.selected = selected;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
}
