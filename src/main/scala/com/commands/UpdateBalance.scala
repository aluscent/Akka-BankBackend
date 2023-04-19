package com.commands

import akka.actor.typed.ActorRef
import com.responses.Response

case class UpdateBalance(id: String, currency: String, amount: Double, replyTo: ActorRef[Response]) extends Command
