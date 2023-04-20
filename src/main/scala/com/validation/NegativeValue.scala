package com.validation

case class NegativeValue(fieldName: String) extends ValidationFailure {
  override def errorMessage: String = s"Field '$fieldName' has negative value."
}
