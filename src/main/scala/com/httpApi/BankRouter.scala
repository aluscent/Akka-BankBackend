package com.httpApi

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.Location
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import cats.data.Validated
import com.commands._
import com.requests.{BankAccountCreationRequest, BankAccountUpdateRequest}
import com.responses._
import com.validation.Validator
import com.validation.Validation.validateEntity
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.Future

class BankRouter(bank: ActorRef[Command])(implicit timeout: Timeout, system: ActorSystem[_]) {
  private def createAccountRequestToCommand(request: BankAccountCreationRequest): Future[Response] =
    bank.ask(request.toCommand)

  private def getAccountRequestToCommand(id: String): Future[Response] =
    bank.ask(GetBankAccount(id, _))

  private def updateAccountRequestToCommand(request: BankAccountUpdateRequest): Future[Response] =
    bank.ask(request.toCommand)

  private def validateRequest[R: Validator](request: R)(routeIfValid: Route): Route =
    validateEntity(request) match {
      case Validated.Valid(_) => routeIfValid
      case Validated.Invalid(e) => complete(StatusCodes.BadRequest, e.toString())
    }

  /*
  POST: /bank/
    payload = account creation req
    response = {
      201
      location = /bank/uuid
    }

  GET: /bank/uuid/
    response = {
      200
      bank account detail
    } or {
      404
      Not found
    }

  PUT: /bank/uuid/
    payload = (currency, amount)
    response = {
      200
      new bank account
    } or {
      404
      Not found
    } or {
      400
      bad request
    }

   */
  import akka.http.scaladsl.server.Directives._

  val route: Route = pathPrefix("bank") {
    pathEndOrSingleSlash {
      post {
        // parse the payload
        entity(as[BankAccountCreationRequest]) { request =>
          validateRequest(request) {

            /*
           - convert req into command
           - send a command to the bank and expect reply
           - send back http response
           */
            onSuccess(createAccountRequestToCommand(request)) {
              case BankAccountCreatedResponse(id) =>
                respondWithHeader(Location(s"/bank/$id")) {
                  complete(StatusCodes.Created, s"Bank account created with ID: $id")
                }
            }
          }
        }
      } ~ put {
        entity(as[BankAccountUpdateRequest]) { request =>
          validateRequest(request) {
            onSuccess(updateAccountRequestToCommand(request)) {
              case BankAccountBalanceUpdatedResponse(Some(value)) =>
                complete(value)
              case BankAccountBalanceUpdatedResponse(None) =>
                complete(StatusCodes.NotFound, s"Bank account '${request.id}' not found.")
            }
          }
        }
      }
    } ~ path(Segment) { id =>
      /*
      - send command to bank and expect reply
      - send back http response
       */
      get {
        onSuccess(getAccountRequestToCommand(id)) {
          case GetBankAccountResponse(Some(value)) =>
            complete(value)
          case GetBankAccountResponse(None) =>
            complete(StatusCodes.NotFound, s"Bank account '$id' not found.")
        }
      }
    }
  }
}
