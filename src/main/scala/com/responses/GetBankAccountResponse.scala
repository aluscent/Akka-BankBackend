package com.responses

import com.states.BankAccount

case class GetBankAccountResponse(maybeBankAccount: Option[BankAccount]) extends Response
