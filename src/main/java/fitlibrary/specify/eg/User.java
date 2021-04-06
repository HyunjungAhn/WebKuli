/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
*/
package fitlibrary.specify.eg;

import java.util.ArrayList;
import java.util.List;

public class User {
	private String name;
	private double owe;
	private List phones = new ArrayList();
	private Account account;
	
	public User() {
		//
	}
	public User(String name, double owe) {
		setName(name);
		setOwe(owe);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getOwe() {
		return owe;
	}
	public void setOwe(double owing) {
		this.owe = owing;
	}
	public List getPhones() {
		return phones;
	}
	public void setPhones(List phones) {
		this.phones = phones;
	}
	public void addPhone(int country, int region, int number) {
		phones.add(new Phone(country,region,number));
	}
	public Account getAccount() {
		return account;
	}
	public void setAccount(Account account) {
		this.account = account;
	}
	public void addDebt(double amount) {
		owe += amount;
	}
	public String toString() {
		return "User["+name+","+owe+"]";
	}
}
