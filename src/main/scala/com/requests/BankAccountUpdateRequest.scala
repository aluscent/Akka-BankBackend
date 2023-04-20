package com.requests

import cats.implicits._
import akka.actor.typed.ActorRef
import com.commands.{Command, UpdateBalance}
import com.responses.Response
import com.validation.Validation._
import com.validation.Validator

case class BankAccountUpdateRequest(id: String, currency: String, amount: Double) extends Requests {
  override def toCommand(replyTo: ActorRef[Response]): Command =
    UpdateBalance(id = id, currency = currency, amount = amount, replyTo = replyTo)
}

object BankAccountUpdateRequest {
  implicit val validator = new Validator[BankAccountUpdateRequest] {
    override def validate(request: BankAccountUpdateRequest): ValidationResult[BankAccountUpdateRequest] = {
      val idValidation = validateRequired(request.id, "id")
      val currencyValidation = validateRequired(request.currency, "currency")
      val amountValidation = request.amount.validNel

      (idValidation, currencyValidation, amountValidation).mapN(BankAccountUpdateRequest.apply)
    }
  }
}
