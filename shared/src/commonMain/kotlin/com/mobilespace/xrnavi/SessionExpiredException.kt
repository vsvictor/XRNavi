package com.mobilespace.xrnavi

/** The stored session cannot be used any more and the user must sign in again. */
internal class SessionExpiredException : Exception("Session expired")
