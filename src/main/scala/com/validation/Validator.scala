package com.validation

import com.validation.Validation.ValidationResult

// general TC for requests
trait Validator[A] {
  def validate(value: A): ValidationResult[A]
}
