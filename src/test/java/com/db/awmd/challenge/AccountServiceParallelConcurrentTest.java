package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.InsufficientFundsException;
import com.db.awmd.challenge.exception.InvalidAccountException;
import com.db.awmd.challenge.exception.InvalidAmmountException;
import com.db.awmd.challenge.repository.AccountsRepositoryInMemory;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.EmailNotificationService;
import com.db.awmd.challenge.service.LockException;

/**
 * @author <b>Yogesh Joshi</b>
 * </br>This test class is intended to test concurrent behaviour of transfer method along with single threaded negative test.
 * </br>Parallel test configuration is done in build.gradle ,it has test.maxParallelForks initiated with number of processors and for concurrent test 
 * i.e invoke same test with multiple thread concurrent-junit utility is used.
 * </br>Parallel-concurrent test is mixed with single threaded negative test.
 * </br>Positive methods validation is done in teardown to validate concurrent thread behaviour. 
 * </br>To simplify parallel-concurrent test and avoid calculation two methods has written which transfers same amount in opposite order by same number of threads so in tear down balance should be same as initial balance.
 * <br>To test LockException,2 threads will invoke transfer amount between same account with 0 time out which is equivalent to trylock without time out then second thread should throw LockException as both thread are parallel and concurrent. 
 */
@RunWith(ConcurrentTestRunner.class)
@SpringBootTest
public class AccountServiceParallelConcurrentTest {

	

	private final String notExistingAccount = "Id-000";
	private final String Account1 = "Id-124";
	private final String Account2 = "Id-125";
	private final String Account3 = "Id-126";
	private final String Account4 = "Id-127";
	private final String Account5 = "Id-128";
	private final String Account6 = "Id-129";
	TimeUnit timeUnit = TimeUnit.MILLISECONDS;
	private static AtomicInteger transferAmountCounter = new AtomicInteger(0);
	private static AtomicInteger transferAmountOppositeCounter = new AtomicInteger(
			0);
	private static AtomicInteger transferNegativeTestCounter = new AtomicInteger(
			0);
	final int threadcount =2000;
	final long timout = 2900;
	private AccountsService accountsService = new AccountsService(
			new AccountsRepositoryInMemory(),new EmailNotificationService());
	
	
	@Before
	public void setup() throws InvalidAccountException {
		
		this.accountsService.createAccount(new Account(Account1,
				new BigDecimal("8000")));
		
		this.accountsService.createAccount(new Account(Account2,
				new BigDecimal("16000")));
		
		this.accountsService.createAccount(new Account(Account3,
				new BigDecimal("20")));
		
		this.accountsService.createAccount(new Account(Account4,
				new BigDecimal("50")));
		this.accountsService.createAccount(new Account(Account5,
				new BigDecimal("9000")));
		this.accountsService.createAccount(new Account(Account6,
				new BigDecimal("17000")));
		
		assertEquals(this.accountsService.getAccount(Account1).getBalance(),
				new BigDecimal(8000));
		assertEquals(this.accountsService.getAccount(Account2).getBalance(),
				new BigDecimal(16000));
		assertEquals(this.accountsService.getAccount(Account3).getBalance(),
				new BigDecimal(20));
		assertEquals(this.accountsService.getAccount(Account4).getBalance(),
				new BigDecimal(50));

	}
	@Test
	public void whenNegativeAmountTransferthenThrowsInvalidAmmountException()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmmountException, LockException {

		try {
			transferNegativeTestCounter.incrementAndGet();	
			this.accountsService.transferAmount(Account2, Account1,
					new BigDecimal("-1"), timout, timeUnit);
			fail("InvalidAmmountException should have thrown when transfering negative Amount");
		} catch (InvalidAmmountException e) {
			assertThat(e.getMessage()).isEqualTo(
					"Amount should not be negative");
		}
	}

	@Test
	public void whenAmountTransferWithNullFromAccountthenThrowsInvalidAccountException()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmmountException, LockException {
		try {
			transferNegativeTestCounter.incrementAndGet();
			this.accountsService.transferAmount(null, Account2, new BigDecimal(
					"1"), timout, timeUnit);
			fail("InvalidAccountException should have thrown when trasnffering with FromAccount as null");
		} catch (InvalidAccountException e) {
			assertThat(e.getMessage()).isEqualTo(
					"Account can not be null or empty");
		}
	}

	@Test
	public void whenAmountTransferWithNullToAccountthenThrowsInvalidAccountException()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmmountException, LockException {
		try {
			transferNegativeTestCounter.incrementAndGet();
			this.accountsService.transferAmount(Account1, null, new BigDecimal(
					"1"), timout, timeUnit);
			fail("InvalidAccountException should have thrown when trasnffering with ToAccount as null");
		} catch (InvalidAccountException e) {
			assertThat(e.getMessage()).isEqualTo(
					"Account can not be null or empty");
		}
	}

	@Test
	public void whenAmountTransferWithEmptyFromAccountthenThrowsInvalidAccountException()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmmountException, LockException {
		try {
			transferNegativeTestCounter.incrementAndGet();
			this.accountsService.transferAmount("", Account2, new BigDecimal(
					"1"), timout, timeUnit);
			fail("InvalidAccountException should have thrown when trasnffering with empty FromAccount");
		} catch (InvalidAccountException e) {
			assertThat(e.getMessage()).isEqualTo(
					"Account can not be null or empty");
		}
	}

	@Test
	public void whenAmountTransferWithEmptyToAccountthenThrowsInvalidAccountException()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmmountException, LockException {
		try {
			transferNegativeTestCounter.incrementAndGet();
			this.accountsService.transferAmount(Account1, "", new BigDecimal(
					"1"), timout, timeUnit);
			fail("InvalidAccountException should have thrown when trasnffering with empty ToAccount");
		} catch (InvalidAccountException e) {
			assertThat(e.getMessage()).isEqualTo(
					"Account can not be null or empty");
		}
	}

	@Test
	public void whenAmountTransferWithInvalidFromAccountthenThrowsInvalidAccountException()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmmountException, LockException {
		try {
			transferNegativeTestCounter.incrementAndGet();
			this.accountsService.transferAmount(notExistingAccount, Account2,
					new BigDecimal("1"), timout, timeUnit);
			fail("InvalidAccountException should have thrown when trasnffering with not existing FromAccount");
		} catch (InvalidAccountException e) {
			assertThat(e.getMessage()).isEqualTo(
					"Account with id : " + notExistingAccount
							+ " does not exist");
		}
	}

	@Test
	public void whenAmountTransferWithInvalidToAccountthenThrowsInvalidAccountException()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmmountException, LockException {
		try {
			transferNegativeTestCounter.incrementAndGet();
			this.accountsService.transferAmount(Account1, notExistingAccount,
					new BigDecimal("1"), timout, timeUnit);
			fail("InvalidAccountException should have thrown when trasnffering with not existing ToAccount");
		} catch (InvalidAccountException e) {
			assertThat(e.getMessage()).isEqualTo(
					"Account with id : " + notExistingAccount
							+ " does not exist");
		}
	}

	@Test
	public void whenTransferAmountIsGreaterThenAccountBalanceThenThrowsInsufficientFundsException()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmmountException, LockException {

		try {
			transferNegativeTestCounter.incrementAndGet();
			this.accountsService.transferAmount(Account3, Account4,
					new BigDecimal("21"), timout, timeUnit);
			fail("InsufficientFundsException should have thrown when transfering Amount greater then balance");
		} catch (InsufficientFundsException e) {
			assertThat(e.getMessage())
					.isEqualTo(
							"withdrawn amount :"
									+ 21
									+ " is greater then Account balanace : "
									+ accountsService.getAccount(Account3)
											.getBalance());
		}
	}

	/** whenTransferAmountConcurrentlyFromAccToToAccThenInTearDownAccountSholdHaveSameValue is transferring amount 1 
	 * from_account to to_account with multiple threads same amount is transffered in opposite order in next test method.
	 * @throws InsufficientFundsException
	 * @throws InterruptedException
	 * @throws InvalidAccountException
	 * @throws InvalidAmmountException
	 * @throws LockException
	 */
	@Test
	@ThreadCount(threadcount)
	public void whenTransferAmountConcurrentlyFromAccToToAccThenInTearDownAccountSholdHaveSameValue()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmmountException, LockException {
		transferAmountCounter.incrementAndGet();
		this.accountsService.transferAmount(Account1, Account2, new BigDecimal(
				"1"), timout, timeUnit);
	}

	/** whenTransferAmountConcurrentlyToAccToFromAccThenInTearDownAccountSholdHaveSameValue is transferring amount 1 in
	 * opposite order of above test method to make test simple.
	 * @throws InsufficientFundsException
	 * @throws InterruptedException
	 * @throws InvalidAccountException
	 * @throws InvalidAmmountException
	 * @throws LockException
	 */
	@Test
	@ThreadCount(threadcount)
	public void whenTransferAmountConcurrentlyToAccToFromAccThenInTearDownAccountSholdHaveSameValue()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmmountException, LockException {
		transferAmountOppositeCounter.incrementAndGet();		
		
			this.accountsService.transferAmount(Account2, Account1, new BigDecimal(
					"1"), timout, timeUnit);
		
	}

	/** whenTransferAmountConcurrentlyFromAccToToAccWithZeroTimeoutThenThrowLockException 2 threads are trying 
	 * invoke transfer amount between same account with 0 time out which is equivalent to trylock without time out then second 
	 * thread should throw LockException as both thread are parallel and concurrent. 
	 * @throws InsufficientFundsException
	 * @throws InterruptedException
	 * @throws InvalidAccountException
	 * @throws InvalidAmmountException
	 * @throws LockException
	 */
	@Test
	@ThreadCount(2)
	public void whenTransferAmountConcurrentlyFromAccToToAccWithZeroTimeoutThenThrowLockException()
			throws InsufficientFundsException, InterruptedException,
			InvalidAccountException, InvalidAmmountException, LockException {
		try {
			transferNegativeTestCounter.incrementAndGet();			
			this.accountsService.transferAmount(Account5, Account6,
					new BigDecimal("1"), 0, timeUnit);
		} catch (LockException e) {	
			System.out.println("LockException occured in thread : "+Thread.currentThread().getName());
			assertThat(e.getMessage())
					.isEqualTo(
							"Unable to acquire locks on the accounts with given timeout : " + 0);
		}
	}

	/** validating account balance specially for Account1 and Account2 which is concurrently 
	 * Transferring amount by 2 method in opposite order(to make test simple and avoid calculations) by 8000 thread in parallel mode by multiple processor(as configured in build.gradle)
	 * so that end of the execution account should have same balance as initial balance.
	 * </br>Account3 and Account4 should have same balance as initial balance as used for insufficient balance negative test case hence amount should not be transfered.
	 * </br>Transfer between Account5 and Account6 should have done by one thread and lock exception should be thrown by other thread so Account5 balance should be 8999 less and Account6 balance should be 17001.
	 * </br>Individual counter are created for councurrent transfer , opposite transfer and negative method to validate number of thread generated by concurrent-junit utility.
	 * 
	 * @throws InvalidAccountException
	 * @throws InterruptedException
	 */
	@After
	public void tearDown() throws InvalidAccountException, InterruptedException {

		assertEquals(this.accountsService.getAccount(Account1).getBalance(),
				new BigDecimal(8000));
		assertEquals(this.accountsService.getAccount(Account2).getBalance(),
				new BigDecimal(16000));
		assertEquals(this.accountsService.getAccount(Account3).getBalance(),
				new BigDecimal(20));

		assertEquals(this.accountsService.getAccount(Account4).getBalance(),
				new BigDecimal(50));

		assertThat(this.accountsService.getAccount(Account5).getBalance().intValue()==
				new BigDecimal(8999).intValue());

		assertThat(this.accountsService.getAccount(Account6).getBalance().intValue()==
				new BigDecimal(17001).intValue());
		assertThat(transferAmountCounter.get()==threadcount);
		assertThat(transferAmountOppositeCounter.get()==threadcount);
		assertThat(transferNegativeTestCounter.get()==10);
		


	}
}
