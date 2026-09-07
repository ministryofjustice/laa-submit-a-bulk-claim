INSERT INTO claims.validation_message_log (
  id,
  submission_id,
  claim_id,
  type,
  source,
  display_message,
  created_on
) VALUES (
  ?::uuid,
  ?::uuid,
  ?::uuid,
  'WARNING',
  ?,
  ?,
  now()
);
