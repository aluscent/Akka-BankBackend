package com.requests

import akka.actor.typed.ActorRef
import com.commands.{Command, CreateBankAccount}
import com.responses.Response
import com.validation.Validation._
import com.validation.Validator
import cats.implicits._

case class BankAccountCreationRequest(user: String, currency: String, balance: Double) extends Requests {
  override def toCommand(replyTo: ActorRef[Response]): Command =
    CreateBankAccount(user = user, currency = currency, initialBalance = balance, replyTo = replyTo)
}

object BankAccountCreationRequest {
  implicit val validator: Validator[BankAccountCreationRequest] = new Validator[BankAccountCreationRequest] {
    override def validate(request: BankAccountCreationRequest): ValidationResult[BankAccountCreationRequest] = {
      val userValidation = validateRequired(request.user, "user")
      val currencyValidation = validateRequired(request.currency, "currency")
      val balanceValidation = validateMinimum(request.balance, 5.0, "balance")

      (userValidation, currencyValidation, balanceValidation)
        .mapN(BankAccountCreationRequest.apply)
    }
  }
}
