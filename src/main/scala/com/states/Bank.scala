package com.states

import akka.actor.typed.ActorRef
import com.commands.Command

case class Bank(accounts: Map[String, ActorRef[Command]])
