package com.db.awmd.challenge.repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import com.db.awmd.challenge.exception.InvalidAccountException;

@Repository
@Slf4j
public class AccountsRepositoryInMemory implements AccountsRepository {

	private final Map<String, Account> accounts = new ConcurrentHashMap<>();

	@Override
	public void createAccount(Account account)
			throws DuplicateAccountIdException {
		Account previousAccount = accounts.putIfAbsent(account.getAccountId(),
				account);
		if (previousAccount != null) {
			throw new DuplicateAccountIdException("Account id "
					+ account.getAccountId() + " already exists!");
		}
	}

	@Override
	public Account getAccount(String accountId) throws InvalidAccountException {
		if(StringUtils.isEmpty(accountId)){
			InvalidAccountException invalidAccountException=new InvalidAccountException("Account can not be null or empty");
			log.error(invalidAccountException.getMessage(),invalidAccountException);
			throw invalidAccountException;
		}
		Account account=accounts.get(accountId);
		if (account == null){
			InvalidAccountException invalidAccountException=new InvalidAccountException("Account with id : "+accountId+ " does not exist");
			log.error(invalidAccountException.getMessage(),invalidAccountException);
			throw invalidAccountException;
		}			
		return account;
	}

	@Override
	public void clearAccounts() {
		accounts.clear();
	}

}
