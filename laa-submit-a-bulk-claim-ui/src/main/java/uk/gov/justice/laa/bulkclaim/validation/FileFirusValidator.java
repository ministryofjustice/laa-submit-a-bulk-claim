package uk.gov.justice.laa.bulkclaim.validation;

import org.springframework.validation.Validator;

/**
 * Represents a validator responsible for performing virus checks on files intended for bulk
 * submissions. This interface extends the {@link Validator} interface for general validation
 * purposes.
 *
 * @author Jamie Briggs
 */
public interface FileFirusValidator extends Validator {}
