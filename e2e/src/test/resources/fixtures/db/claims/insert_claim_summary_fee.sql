INSERT INTO claims.claim_summary_fee (
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
