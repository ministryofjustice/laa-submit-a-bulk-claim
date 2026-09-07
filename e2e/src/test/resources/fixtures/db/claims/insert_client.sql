INSERT INTO claims.client (
  id,
  claim_id,
  created_by_user_id,
  created_on
) VALUES (
  ?::uuid,
  ?::uuid,
  ?,
  now()
);
