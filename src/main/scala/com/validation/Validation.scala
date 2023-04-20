package com.validation

import cats.data.ValidatedNel
import cats.implicits._

object Validation {
  // TC instances
  implicit val minimumInt: Minimum[Int] = _ >= _
  implicit val minimumDouble: Minimum[Double] = _ >= _
  implicit val requiredString: Required[String] = _.nonEmpty

  // usage
  private def required[A](value: A)(implicit _required: Required[A]): Boolean = _required(value)
  private def minimum[A](value: A, threshold: Double)(implicit _minimum: Minimum[A]): Boolean = _minimum(value, threshold)


  // validated
  type ValidationResult[A] = ValidatedNel[ValidationFailure, A]


  // main API
  def validateMinimum[A: Minimum](value: A, threshold: Double, fieldName: String): ValidationResult[A] =
    if (minimum(value, threshold)) value.validNel
    else if (threshold == 0) NegativeValue(fieldName).invalidNel
    else BelowMinimumValue(fieldName, threshold).invalidNel

  def validateRequired[A: Required](value: A, fieldName: String): ValidationResult[A] =
    if (required(value)) value.validNel
    else EmptyField(fieldName).invalidNel

  def validateEntity[A](value: A)(implicit validator: Validator[A]): ValidationResult[A] = validator.validate(value)
}
