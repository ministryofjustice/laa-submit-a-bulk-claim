INSERT INTO claims.matter_start (
  id,
  submission_id,
  number_of_matter_starts,
  category_code,
  mediation_type,
  created_by_user_id,
  created_on
) VALUES (
  ?::uuid,
  ?::uuid,
  ?,
  ?,
  ?,
  ?,
  now()
);
