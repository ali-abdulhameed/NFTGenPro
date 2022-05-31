package utils;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

public class ViewSection extends Component {
	private Map<String, Component> components;
	private JPanel panel;
	
	public ViewSection() {
		components = new HashMap<>();
		panel = new JPanel();
	}
	
	public Map<String, Component> getComponents() {
		return components;
	}
	
	public <T extends Component> T getComponent(String name) {
		return (T) components.get(name);
	}
	
	public <T extends Component> void addComponent(String name, T component, boolean add) {
		if(add) panel.add(component);
		if(name != null) components.put(name, component);
		refresh();
	}
	
	public void removeComponent(String name) {
		Component c = components.remove(name);
		if(c != null) panel.remove(c);
		refresh();
	}
	
	public void removeComponent(Component c) {
		if(c != null) panel.remove(c);
		refresh();
	}
	
	public void refresh() {
		panel.revalidate();
		panel.repaint();
	}
	public JPanel getPanel() {
		return panel;
	}
}
