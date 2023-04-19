package com.commands

import akka.actor.typed.ActorRef
import com.responses.Response

case class CreateBankAccount(user: String, currency: String, initialBalance: Double, replyTo: ActorRef[Response]) extends Command
