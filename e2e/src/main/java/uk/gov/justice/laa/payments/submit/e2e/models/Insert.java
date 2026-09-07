package uk.gov.justice.laa.payments.submit.e2e.models;

import java.util.List;

public interface Insert {

  String table();

  String id();

  List<Object> parameters();
}
