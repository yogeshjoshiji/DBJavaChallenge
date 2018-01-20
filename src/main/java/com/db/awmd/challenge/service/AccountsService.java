package com.db.awmd.challenge.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.exception.InvalidAmmountException;
import com.db.awmd.challenge.repository.AccountsRepository;

@Service
@Validated
@Slf4j
public class AccountsService {	

	@Getter
	private  AccountsRepository accountsRepository;	
	
	@Autowired	
	NotificationService notificationService;
	
	public AccountsService(){
		
	}
	

	@Autowired
	public AccountsService(AccountsRepository accountsRepository,NotificationService notificationService) {
		this.accountsRepository = accountsRepository;
		this.notificationService=notificationService;
	}

	public void createAccount(Account account) {
		this.accountsRepository.createAccount(account);
	}

	public Account getAccount(String accountId) throws InvalidAccountException {
		return this.accountsRepository.getAccount(accountId);
	}
	public void clearAccounts() {
		accountsRepository.clearAccounts();
	}

	/** transferAmount will do initial validation on account(account should be valid and should exist) and amount(should be positive.) then called transfer method to intiate the transfer.
	 * @param fromAccountId
	 * @param toAccountId
	 * @param amount
	 * @param timeout
	 * @param unit
	 * @throws InsufficientFundsException
	 * @throws InterruptedException
	 * @throws InvalidAccountException
	 * @throws InvalidAmmountException
	 * @throws LockException
	 */
	public boolean transferAmount(
			String fromAccountId,
			String toAccountId,
			 BigDecimal amount,
			long timeout, TimeUnit unit) throws InsufficientFundsException,
			InterruptedException, InvalidAccountException,
			InvalidAmmountException, LockException {
		Account fromAccount = accountsRepository.getAccount(fromAccountId);
		Account toAccount = accountsRepository.getAccount(toAccountId);
		if (checkAmountIsNotNegative(amount)) {
			return this.transfer(fromAccount, toAccount, amount, timeout, unit);
		}
		return false;
	}
	
	
	/** transfer method will first acquire lock on both the accounts in particular order then invoke transferAmount to withdraw and deposit the amount.
	 * @param fromAccount
	 * @param toAccount
	 * @param amount
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InsufficientFundsException
	 * @throws InterruptedException
	 * @throws InvalidAmmountException
	 * @throws LockException
	 */
	private boolean transfer(Account fromAccount, Account toAccount,
			BigDecimal amount, long timeout, TimeUnit unit)
			throws InsufficientFundsException, InterruptedException, InvalidAmmountException, LockException {

		final Account[] accounts = new Account[] { fromAccount, toAccount };
		Arrays.sort(accounts);

		Lock fromAcctLock = accounts[0].getMonitor();
		Lock toAcctLock = accounts[1].getMonitor();

		if (fromAcctLock.tryLock(timeout, unit)) {
			try {
				if (toAcctLock.tryLock(timeout, unit)) {
					try {
						return transferAmount(fromAccount, toAccount, amount,
								timeout, unit);
					} finally {
						toAcctLock.unlock();
					}
				}
			} finally {
				fromAcctLock.unlock();
				
			}

		}
		LockException LockException=new LockException("Unable to acquire locks on the accounts with given timeout : "+timeout);
		log.error(LockException.getMessage(), LockException);
		throw LockException;

	}	
	/** transferAmount atomically transfer amount between account already holding lock and will rollback in case Withdrawal is successful and Deposit is unsuccessful.
	 * @param fromAccount
	 * @param toAccount
	 * @param amount
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InsufficientFundsException
	 * @throws InterruptedException
	 * @throws InvalidAmmountException
	 */
	private boolean transferAmount(Account fromAccount, Account toAccount,
			BigDecimal amount, long timeout, TimeUnit unit)
			throws InsufficientFundsException, InterruptedException, InvalidAmmountException {
		boolean deposit = false;
		boolean withdrawn = false;
		boolean success = false;
		
		try {
			if (this.withdrawAmountFromAcoountWithGivenTimeout(fromAccount, amount, timeout, unit)) {
				withdrawn = true;
				if (this.depositAmountToAcoountWithGivenTimeOut(toAccount, amount, timeout, unit)) {					
					//sendNotification(fromAccount, "amount : "+amount.intValue()+" is transffered from account : "+fromAccount.getAccountId()+" to account : "+toAccount.getAccountId());
					notificationService.notifyAboutTransfer(fromAccount, "amount : "+amount.intValue()+" is transffered from account : "+fromAccount.getAccountId()+" to account : "+toAccount.getAccountId());
					/*printinfo(amount, fromAccount.getAccountId(),
							toAccount.getAccountId(), fromAccount.getBalance(),
							toAccount.getBalance());*/
					deposit = true;
				}
				
				success = true;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (withdrawn && !deposit) {
				if(log.isDebugEnabled()){
					log.debug("Withdrawl is successfull whereas deposit is unsuccessful so rollbacking the operation");
				}				
				this.depositAmountToAcoountWithGivenTimeOut(fromAccount, amount, timeout, unit);
			}
				
			
		}
		
		return success;
	}
	
	/** will deposit the amount to given account in given timeout by acquiring the lock on account to avoid operation on stale value.
	 * as this method can be called independently so again taking Reentrant lock on account.
	 * @param account
	 * @param amount
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InterruptedException
	 * @throws InvalidAmmountException
	 */
	public boolean depositAmountToAcoountWithGivenTimeOut(Account account, final BigDecimal amount,
			long timeout, TimeUnit unit) throws InterruptedException,
			InvalidAmmountException {
		boolean success = false;
		// below check is called redundantly(already called in transferAmount method)  as this method can be called independently.
		if (checkAmountIsNotNegative(amount)) {
			if (account.getMonitor().tryLock(timeout, unit)) {
				try {
					account.setBalance(account.getBalance().add(amount));
					success = true;
				} finally { // In case there was an Exception we're covered
					account.getMonitor().unlock();
				}
			}
		}
		return success;
	}

	/** withdrawAmountFromAcoountWithGivenTimeout will withdraw the amount to given account in given timeout by acquiring the lock on account to avoid operation on stale value.
	 * as this method can be called independently so again taking Reentrant lock on account.
	 * @param account
	 * @param amount
	 * @param timeout
	 * @param unit
	 * @return
	 * @throws InsufficientFundsException
	 * @throws InterruptedException
	 * @throws InvalidAmmountException
	 */
	public boolean withdrawAmountFromAcoountWithGivenTimeout(Account account, final BigDecimal amount,
			long timeout, TimeUnit unit) throws InsufficientFundsException,
			InterruptedException, InvalidAmmountException {
		boolean success = false;
		// below check is called redundantly(already called in transferAmount method) as this method can be called independently.
		if (checkAmountIsNotNegative(amount)) {
			if (account.getMonitor().tryLock(timeout, unit)) {
				try {
					if (account.getBalance().compareTo(amount) < 0){
						InsufficientFundsException insufficientFundsException= new InsufficientFundsException(
								"withdrawn amount :"
										+ amount
										+ " is greater then Account balanace : "
										+ account.getBalance());
						log.error(insufficientFundsException.getMessage(),insufficientFundsException);
						throw insufficientFundsException;
					}
						
					else {
						account.setBalance(account.getBalance()
								.subtract(amount));						
						success = true;
					}

				} finally {
					account.getMonitor().unlock();
				}
			}
		}
		return success;
	}
	/** sendNotification is used to send Email notification asynchronously can used to send other notification i.e. SMS notification as well.
	 * @param account
	 * @param transferDescription
	 * @throws InterruptedException
	 */
	/*@Async("threadPoolTaskExecutor")
	//@Async()
	public void sendNotification(Account account,String transferDescription) throws InterruptedException{			
		notificationService.notifyAboutTransfer(account, transferDescription);
		System.out.println("thread is : "+Thread.currentThread().getName());		
	}*/
	/** check if amount is not negative else throw InvalidAmmountException with message Amount should not be negative
	 * @param amount
	 * @return
	 * @throws InvalidAmmountException
	 */
	private boolean checkAmountIsNotNegative(BigDecimal amount)
			throws InvalidAmmountException {
		if (amount.compareTo(BigDecimal.ZERO) > 0) {
			return true;
		} else {
			InvalidAmmountException invalidAmmountException = new InvalidAmmountException("Amount should not be negative");
			log.error(invalidAmmountException.getMessage(),invalidAmmountException);
			throw invalidAmmountException;
		}
	}
	
	/** printinfo is used for test purpose to see thread names and update account balance after succeful transfer.
	 * @param amount
	 * @param fromAccountID
	 * @param toAccountID
	 * @param fromAccountBal
	 * @param toAccountBal
	 */
	static void printinfo(final BigDecimal amount, final String fromAccountID,
			final String toAccountID, final BigDecimal fromAccountBal,
			final BigDecimal toAccountBal) {
		//String message = "%s transfered %d from %s to %s. From Account balance is : %d and To Account balance is : %d\n";
		String message = "{} transfered {} from {} to {} is successful. From Account balance is : {} and To Account balance is : {}\n";
		String threadName = Thread.currentThread().getName();
		if(log.isDebugEnabled()){
			log.debug(message, threadName, amount.intValue(),
					fromAccountID, toAccountID, fromAccountBal.intValue(),
					toAccountBal.intValue());
		}
		/*System.out.printf(message, threadName, amount.intValue(),
				fromAccountID, toAccountID, fromAccountBal.intValue(),
				toAccountBal.intValue());*/
	}

}
