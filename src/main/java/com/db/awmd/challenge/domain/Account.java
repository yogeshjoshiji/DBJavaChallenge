package com.db.awmd.challenge.domain;

import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class Account implements Comparable<Account> {

	@ApiModelProperty(hidden = true)
	public Lock monitor = new ReentrantLock(true);
	
	@NotEmpty(message="Account Id cannot be null or empty")
	private final String accountId;

	@NotNull(message = "Initial balance can not be null")
	@Min(value = 0, message  = "Initial balance must be positive")
	private BigDecimal balance;

	public Account(String accountId) {
		this.accountId = accountId;
		this.balance = BigDecimal.ZERO;
	}

	@JsonCreator
	public Account(@JsonProperty("accountId") String accountId,
			@JsonProperty("balance") BigDecimal balance) {
		this.accountId = accountId;
		this.balance = balance;
	}
	
	public int compareTo(final Account other) {
		return new Integer(hashCode()).compareTo(other.hashCode());
	}
}
