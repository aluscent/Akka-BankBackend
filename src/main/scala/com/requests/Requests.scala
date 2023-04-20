package com.requests

import akka.actor.typed.ActorRef
import com.commands.Command
import com.responses.Response

trait Requests {
  def toCommand(replyTo: ActorRef[Response]): Command
}
