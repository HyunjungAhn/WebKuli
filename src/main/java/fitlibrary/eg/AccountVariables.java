package fitlibrary.eg;

import java.util.ArrayList;
import java.util.List;

public class AccountVariables extends DomainAdapterWithVariables {
	private AccountsRepository accountsRepository = new AccountsRepository();

	public Object getSystemUnderTest() {
		return null;
	}
	public boolean createAccountNamedWithId(String accountName, Variable idVariable) {
		idVariable.setValue(accountsRepository.create(accountName));
		return true;
	}
	public boolean addToAccount(float amount, Variable idVariable) {
		account(idVariable).add(amount);
		return true;
	}
	public Account account(Variable idVariable) { 
		return accountsRepository.getAccount(((Integer)idVariable.getValue()).intValue());
	}
	
	public static class AccountsRepository {
		private static int NEXT_ID = 0;
		public List accounts = new ArrayList();
		
		public List getAccounts() {
			return accounts;
		}
		public Account getAccount(int id) {
			for (int i = 0; i < accounts.size(); i++) {
				Account account = (Account)accounts.get(i);
				if (account.getId() == id)
					return account;
			}
			return null;
		}
		public Integer create(String accountName) {
			NEXT_ID++;
			accounts.add(new Account(NEXT_ID,accountName));
			return new Integer(NEXT_ID);
		}
		public void setAccounts(List accounts) {
			this.accounts = accounts;
		}
	}
	
	public static class Account {
		private int id;
		private String name;
		private float balance;

		public Account(int id, String name) {
			this.id = id;
			this.name = name;
		}
		public int getId() {
			return id;
		}
		public void add(float balance) {
			this.balance += balance;
		}
	}
}
