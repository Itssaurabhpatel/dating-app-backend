-- Create dating_user role if not exists
DO
$$
BEGIN
   IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'dating_user') THEN
      CREATE ROLE dating_user WITH LOGIN PASSWORD 'dating_pass' SUPERUSER;
   END IF;
END
$$;

-- Create single database with correct ownership
SELECT 'CREATE DATABASE dating_platform OWNER dating_user' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'dating_platform')\gexec
