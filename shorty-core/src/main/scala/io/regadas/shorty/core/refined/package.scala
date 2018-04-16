package io.regadas.shorty.core

import eu.timepit.refined.api.Validate
import org.apache.commons.validator.routines.UrlValidator

package object refined {

  final case class ValidUrl()

  implicit def urlValidate: Validate.Plain[String, ValidUrl] =
    Validate.fromPredicate(new UrlValidator().isValid(_), _.toString, ValidUrl())

}
