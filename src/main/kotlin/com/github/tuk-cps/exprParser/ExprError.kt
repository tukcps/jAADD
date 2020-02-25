package exprParser

class ExprError internal constructor(msg: String) : Exception("Error in Expression: $msg")