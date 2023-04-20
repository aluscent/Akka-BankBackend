package com.validation

// not allow requests with negative balance
trait Minimum[A] extends ((A, Double) => Boolean)
