package uk.gov.justice.laa.bulkclaim.dto;

/**
 * Represents details of an authenticated user.
 *
 * @param preferredUsername the user's preferred username
 * @param firmName the name of the user's firm
 * @param firmCode the code of the user's firm
 */
public record AuthenticatedUserDetails(
    String preferredUsername, String firmName, String firmCode) {}
