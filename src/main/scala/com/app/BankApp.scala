package com.app

import akka.NotUsed
import akka.actor.typed.{ActorRef, ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.actors.PersistentBank
import com.commands.Command
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.Http
import akka.util.Timeout
import com.httpApi.BankRouter

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object BankApp {
  private def startHttpServer(bank: ActorRef[Command])
                             (implicit system: ActorSystem[_], timeout: Timeout, ec: ExecutionContext): Unit = {
    val router = new BankRouter(bank)
    val routes = router.route

    Http()
      .newServerAt("localhost", 8080)
      .bind(routes)
      .onComplete {
        case Failure(exception) =>
          system.log.error(s"Failed to bind HTTP.\nReason: ${exception.getCause}")
          system.terminate()
        case Success(value) =>
          val address = value.localAddress
          system.log.info(s"Server online at: ${address.getHostString}:${address.getPort}")
      }
  }

  private trait RootCommand
  private case class RetrieveBankAccount(replyTo: ActorRef[ActorRef[Command]]) extends RootCommand


  private val behavior: Behavior[RootCommand] = Behaviors.setup { context =>
    val bankActor = context.spawn(PersistentBank(), "bank")

    Behaviors.receiveMessage {
      case RetrieveBankAccount(replyTo) =>
        replyTo ! bankActor
        Behaviors.same
    }
  }

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[RootCommand] = ActorSystem(behavior, "bankSystem")
    implicit val timeout: Timeout = Timeout(3 seconds)
    implicit val ec: ExecutionContext = system.executionContext

    // 500a7a02-599c-442b-921a-2c8cdae5259d, d9f23853-c0c5-452b-9d17-d63e8b40f75c

    val bankActorFuture: Future[ActorRef[Command]] = system.ask(RetrieveBankAccount)
    bankActorFuture foreach startHttpServer
  }

}
