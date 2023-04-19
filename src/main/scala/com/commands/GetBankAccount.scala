package com.commands

import akka.actor.typed.ActorRef
import com.responses.Response

case class GetBankAccount(id: String, replyTo: ActorRef[Response]) extends Command
