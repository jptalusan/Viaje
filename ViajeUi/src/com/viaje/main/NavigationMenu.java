package com.viaje.main;

public class NavigationMenu {
	private int iconMenuID;
	private int stringPageTitle;
	
	public NavigationMenu(int iconMenuID, int stringPageTitle)
	{
		super();
		this.iconMenuID = iconMenuID;
		this.stringPageTitle = stringPageTitle;
	}
	
	public NavigationMenu(int iconMenuID, String stringPageTitleString)
	{
		super();
		this.iconMenuID = iconMenuID;
	}
	
	public int getIconMenuID()
	{
		return iconMenuID;
	}
	
	public int getStringPageTitle()
	{
		return stringPageTitle;
	}
}
