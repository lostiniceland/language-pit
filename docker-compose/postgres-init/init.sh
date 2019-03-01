#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
  revoke all on database postgres from public;
	create user bikes with password '1234';
	create user wife with password '4321';
	grant connect on database postgres to bikes;
  grant connect on database postgres to wife;
	create schema authorization bikes;
	create schema authorization wife;
	alter user bikes set search_path to 'bikes';
	alter user wife set search_path to 'wife';
EOSQL