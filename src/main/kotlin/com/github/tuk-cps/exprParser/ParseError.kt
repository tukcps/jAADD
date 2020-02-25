package exprParser

/**
 * This Exceptions is thrown for all errors that are caused in the parsing methods.
 * It just writes an error message.
 */
class ParseError internal constructor(msg: String) : Exception("Parse Error: $msg")