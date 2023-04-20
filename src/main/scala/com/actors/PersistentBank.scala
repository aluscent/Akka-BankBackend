package com.actors

import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior}
import com.commands._
import com.events.{BankAccountCreated, Event}
import com.responses.{BankAccountBalanceUpdatedResponse, GetBankAccountResponse}
import com.states.{Bank, BankAccount}

import java.util.UUID

class PersistentBank {
  def commandHandler(context: ActorContext[Command]): (Bank, Command) => Effect[Event, Bank] = (bank, command) =>
    command match {
      case message@CreateBankAccount(user, currency, initialBalance, _) =>
        val id = UUID.randomUUID().toString
        println(s"[BANK] Create account: $id")
        val newBankAccount = context.spawn(PersistentBankAccount(id), id)
        Effect
          .persist(BankAccountCreated(BankAccount(id = id, user = user, currency = currency, balance = initialBalance)))
          .thenReply(newBankAccount)(_ => message)

      case message@GetBankAccount(id, replyTo) =>
        println(s"[BANK] Get account balance: $id")
        bank.accounts.get(id) match {
          case Some(value) => Effect.reply(value)(message)
          case None => Effect.reply(replyTo)(GetBankAccountResponse(None))
        }

      case message@UpdateBalance(id, _, _, replyTo) =>
        println(s"[BANK] Update account balance: $id")
        bank.accounts.get(id) match {
          case Some(value) => Effect.reply(value)(message)
          case None => Effect.reply(replyTo)(BankAccountBalanceUpdatedResponse(None))
        }
    }

  def eventHandler(context: ActorContext[Command]): (Bank, Event) => Bank = (bank, event) =>
    event match {
      case BankAccountCreated(bankAccount) =>
        val id = bankAccount.id
        val newAccount: ActorRef[Command] = context.child(id)
          .getOrElse(context.spawn(PersistentBankAccount(id), id))
          .asInstanceOf[ActorRef[Command]]
        bank.copy(accounts = bank.accounts + (id -> newAccount))
    }

  def apply(): Behavior[Command] = Behaviors.setup { context =>
    EventSourcedBehavior[Command, Event, Bank](
      persistenceId = PersistenceId.ofUniqueId("bank"),
      emptyState = Bank(Map.empty[String, ActorRef[Command]]),
      commandHandler = commandHandler(context),
      eventHandler = eventHandler(context)
    )
  }
}

object PersistentBank {
  def apply(): Behavior[Command] = (new PersistentBank).apply()
}
