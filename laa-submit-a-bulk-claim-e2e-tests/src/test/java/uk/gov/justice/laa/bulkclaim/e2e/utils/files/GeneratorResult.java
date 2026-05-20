package uk.gov.justice.laa.bulkclaim.e2e.utils.files;

import java.util.List;

/** TS-like generator return shape: { filePaths, office }. */
public record GeneratorResult(List<String> filePaths, String office) {}

