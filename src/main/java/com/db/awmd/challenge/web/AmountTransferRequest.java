package com.db.awmd.challenge.web;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import lombok.Data;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class AmountTransferRequest {
	
	 @NotNull
	  @NotEmpty
	  private final String fromAccountId;
	 
	 @NotNull
	  @NotEmpty
	  private final String toAccountId;

	  @NotNull
	  @Min(value = 0, message = "Initial balance must be positive.")
	  private BigDecimal amount;



	  @JsonCreator
	  public AmountTransferRequest(@JsonProperty("fromAccountId") String fromAccountId,@JsonProperty("toAccountId") String toAccoountId,@JsonProperty("amount") BigDecimal amount){
	  this.fromAccountId = fromAccountId;
	    this.toAccountId=toAccoountId;
	    this.amount = amount;
	  }
	

}
