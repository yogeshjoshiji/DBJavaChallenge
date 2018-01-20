package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.context.WebApplicationContext;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;
  
  @Autowired
  private WebApplicationContext webApplicationContext;

  @Before
  public void prepareMockMvc() throws Exception {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.clearAccounts();
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
		      .content("{\"accountId\":\"Id-124\",\"balance\":1000}")).andExpect(status().isCreated());
	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
		      .content("{\"accountId\":\"Id-125\",\"balance\":1000}")).andExpect(status().isCreated());
   
    
  }

  @Test
  public void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  public void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andReturn()
      .getResponse()
      .getContentAsString()
      .contains("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}");
  }
  
  
  @Test
  public void when_transfer_Amount_with_no_body_then_return_bad_request_code() throws Exception{	 
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
			  ).andExpect(status().isBadRequest()); 
  }
  
  @Test
  public void When_transfer_amount_from_non_existing_from_account_then_return_bad_request_code() throws Exception{	  
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
			  .content("{\"fromAccountId\":\"Id-126\",\"toAccountId\":\"Id-125\",\"amount\":500}")).andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString().equals("Account Id-126 does not exist");
  }
  @Test
  public void When_transfer_amount_from_non_existing_to_account_then_return_bad_request_code() throws Exception{	 
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
			  .content("{\"fromAccountId\":\"Id-124\",\"toAccountId\":\"Id-126\",\"amount\":500}")).andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString().equals("Account Id-126 does not exist");
  }
  @Test
  public void When_transfer_amount_from_null_from_account_then_return_bad_request_code() throws Exception{	 
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
			  .content("{\"fromAccountId\":null,\"toAccountId\":\"Id-125\",\"amount\":500}")).andExpect(status().isBadRequest());
  }
  @Test
  public void When_transfer_amount_from_null_to_account_then_return_bad_request_code() throws Exception{	 
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
			  .content("{\"fromAccountId\":\"Id-124\",\"toAccountId\":null,\"amount\":500}")).andExpect(status().isBadRequest());
  }
  @Test
  public void When_transfer_amount_from_empty_from_account_then_return_bad_request_code() throws Exception{	 
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
			  .content("{\"fromAccountId\":\"\",\"toAccountId\":\"Id-125\",\"amount\":500}")).andExpect(status().isBadRequest());
  }
  @Test
  public void When_transfer_amount_from_empty_to_account_then_return_bad_request_code() throws Exception{	 
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
			  .content("{\"fromAccountId\":\"Id-124\",\"toAccountId\":\"\",\"amount\":500}")).andExpect(status().isBadRequest());
  }
  @Test
  public void When_transfer_negative_amount_then_return_bad_request_code() throws Exception{
	  ResultActions ra=this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
			  .content("{\"fromAccountId\":\"Id-124\",\"toAccountId\":\"Id-125\",\"amount\":-500}"));
	  System.out.println(ra.andReturn().getResponse().getContentAsString());
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
			  .content("{\"fromAccountId\":\"Id-124\",\"toAccountId\":\"Id-125\",\"amount\":-500}")).andExpect(status().isBadRequest());
  }
  @Test
  public void when_Transfer_Amount_IsGreaterThen_AccountBalance_then_return_bad_request_code() throws Exception{
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
			  .content("{\"fromAccountId\":\"Id-124\",\"toAccountId\":\"Id-125\",\"amount\":2000}")).andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString().equals("withdrawn amount :2000 is greater then Account balanace : 1000");
	  
  }
  @Test
  public void when_Transfer_Amount_ToAcc_To_FromAcc_Then_Updated_Balance_Should_Reflect() throws Exception{
	 
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
			  .content("{\"fromAccountId\":\"Id-124\",\"toAccountId\":\"Id-125\",\"amount\":500}")).andExpect(status().isOk());
	  
	  assertEquals(new BigDecimal(500),this.accountsService.getAccount("Id-124").getBalance());
		 assertEquals(new BigDecimal(1500),this.accountsService.getAccount("Id-125").getBalance());
  }
  
}
