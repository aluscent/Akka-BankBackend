package com.validation

case class EmptyField(fieldName: String) extends ValidationFailure {
  override def errorMessage: String = s"Field '$fieldName' is empty."
}
