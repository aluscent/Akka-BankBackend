package com.validation

// field must be present
trait Required[A] extends (A => Boolean)
