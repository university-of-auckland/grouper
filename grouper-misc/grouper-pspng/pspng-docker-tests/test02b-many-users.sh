#!/bin/bash
#
# description: test02a - group basics 
# configs: ldap-attributes, posix-groups, goun-groups, posix-groups-bushy, posix-groups-bushy-dnsearch
#
# This test takes verifies the correct incremental provisioning is happening while groups are 
# created and memberships are added after the groups are marked
#

set -o errexit
set -o pipefail
set -o nounset

ME=$(basename "$0")

# Functions for test harness: read_test_config_files, test_start, start_docker, etc
# (This also pulls in functions.bash (log_always, temp-file functions, etc)
. "$(dirname "${BASH_SOURCE[0]}")/functions-pspng-testing.bash"


###
# Pull-in shell functions for generating config and verifying provisioning
# These are usually specific for different provisioning endpoints
# And the same 
#   Config Building: output_log4j_properties, output_grouper_loader_properties
#   Hooks: test_is_starting, test_is_ending
#   Provisioning verification: validate_provisioning <group> <correct members (comma-separated, alphabetical)>
# Defines: $flavor
read_test_config_files "${1:-posix-groups}"


# Note that the test is starting, saves start-time, etc
test_start "$ME" "Pspng ($flavor): provisioning groups and membership changes"

################
## CONFIGURE GROUPER
## This will populate and define GROUPER_CONFIG_DIR

create_grouper_daemon_config

################
## START DOCKER

start_docker "${ME}_$flavor"


wait_for_grouper_daemon_to_be_running

create_test_folder

mark_test_folder_for_provisioning

create_group1_and_group2

. get_ldap_properties_from_loader_properties
(run_in_grouper_daemon bash -c "ldapsearch -H ldap://ldap -x -D cn=admin,dc=example,dc=edu -w secret -b dc=example,dc=edu -s sub uid=* uid 2>&1 | grep uid: | noAttributeLabels")>$T/all_users
ALL_USERS=$(sort $T/all_users)
ALL_USERS_CSV=$(tr \\n , <<<"$ALL_USERS" | sed 's/,*$//')

add_members_to_group1 $ALL_USERS
await_changelog_catchup --time_limit_secs 1000

validate_provisioning "$GROUP1_NAME" "$ALL_USERS_CSV"

add_group1_to_group2
await_changelog_catchup

validate_provisioning "$GROUP2_NAME" "$ALL_USERS_CSV"

#make sure extra groups were not provisioned
validate_deprovisioning "$UNPROVISIONED_GROUP_NAME"

wrap_up
assert_empty "$ERRORS" "Check for exceptions in grouper_error.log"
test_success
