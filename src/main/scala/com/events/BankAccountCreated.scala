package com.events

import com.states.BankAccount

case class BankAccountCreated(bankAccount: BankAccount) extends Event
