package com.responses

import com.states.BankAccount

case class BankAccountBalanceUpdatedResponse(maybeBankAccount: Option[BankAccount]) extends Response
