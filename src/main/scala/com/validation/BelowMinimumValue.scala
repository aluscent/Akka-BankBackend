package com.validation

case class BelowMinimumValue(fieldName: String, minimumValue: Double) extends ValidationFailure {
  override def errorMessage: String = s"Field '$fieldName' is below minimum value of $minimumValue."
}
