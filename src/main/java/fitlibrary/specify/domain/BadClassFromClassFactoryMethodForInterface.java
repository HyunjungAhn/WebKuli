/*
 * Copyright (c) 2006 Rick Mugridge, www.RimuResearch.com
 * Released under the terms of the GNU General Public License version 2 or later.
 * Written: 21/09/2006
*/

package fitlibrary.specify.domain;

import fitlibrary.traverse.DomainAdapter;

public class BadClassFromClassFactoryMethodForInterface implements DomainAdapter {
	public void setAbstractUser(AbstractUser user) {
		//
	}
	public interface AbstractUser {
		//
	}
	public static class PrivateUser implements AbstractUser {
		private String name;
		
		private PrivateUser() {
			//
		}
		private String getName() {
			return name;
		}
		private void setName(String name) {
			this.name = name;
		}
	}
	public static class NoNullaryUser implements AbstractUser {
		public NoNullaryUser(int i) {
			//
		}
	}
	public Class concreteClassOfAbstractUser(String typeName) {
		if ("Private".equals(typeName))
			return PrivateUser.class;
		if ("No Nullary".equals(typeName))
			return NoNullaryUser.class;
		if ("String".equals(typeName))
			return String.class;
		return null;
	}
	public Object getSystemUnderTest() {
		return null;
	}

}
