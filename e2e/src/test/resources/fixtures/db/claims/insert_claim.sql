INSERT INTO claims.claim (
  id,
  submission_id,
  status,
  line_number,
  schedule_reference,
  case_reference_number,
  unique_file_number,
  matter_type_code,
  fee_code,
  created_by_user_id,
  created_on
) VALUES (
  ?::uuid,
  ?::uuid,
  'VALID',
  ?,
  ?,
  ?,
  ?,
  ?,
  ?,
  ?,
  now()
);
