package com.actors

import com.commands._
import com.responses._
import com.events._
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import com.states.BankAccount

// a single bank account
class PersistentBankAccount {
  /*
  This project obeys the event-sourcing architecture.
  - it is fault tolerant
  - it can be audited
   */

  // message/command handler => persist an event
  private def commandHandler: (BankAccount, Command) => Effect[Event, BankAccount] = (state, command) =>
    command match {
      case CreateBankAccount(user, currency, initialBalance, replyTo) =>
        val id = state.id
        Effect.persist(BankAccountCreated(BankAccount(id, user = user, currency = currency, balance = initialBalance)))
          .thenReply(replyTo)(_ => BankAccountCreatedResponse(id))
      case GetBankAccount(id, replyTo) =>
        if (state.id == id) Effect.reply(replyTo)(GetBankAccountResponse(Some(state)))
        else Effect.reply(replyTo)(GetBankAccountResponse(None))
      case UpdateBalance(id, _, amount, replyTo) =>
        val newBalance = state.balance + amount
        if (newBalance >= 0 && state.id == id)
          Effect.persist(BalanceUpdated(amount))
            .thenReply(replyTo)(newState => BankAccountBalanceUpdatedResponse(Some(newState)))
        else Effect.reply(replyTo)(BankAccountBalanceUpdatedResponse(None))
    }

  // event handler => update state
  private def eventHandler: (BankAccount, Event) => BankAccount = (state, event) =>
    event match {
      case BalanceUpdated(amount) =>
        state.copy(balance = state.balance + amount)
      case BankAccountCreated(bankAccount) => bankAccount
    }


  def apply(id: String): Behavior[Command] = EventSourcedBehavior[Command, Event, BankAccount](
    persistenceId = PersistenceId.ofUniqueId(id),
    emptyState = BankAccount(id, "", "", 0.0),
    commandHandler = commandHandler,
    eventHandler = eventHandler
  )
}

object PersistentBankAccount {
  def apply(id: String): Behavior[Command] = (new PersistentBankAccount).apply(id)
}
