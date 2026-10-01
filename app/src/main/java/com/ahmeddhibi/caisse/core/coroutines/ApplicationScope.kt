package com.ahmeddhibi.caisse.core.coroutines

import javax.inject.Qualifier

/** Lives as long as the process: for work that must not stop when a screen goes away. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FUNCTION)
annotation class ApplicationScope
