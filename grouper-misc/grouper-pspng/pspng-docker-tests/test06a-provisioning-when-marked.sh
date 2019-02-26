#!/bin/bash
#
# description: test06a - provisioning when groups are marked
# configs: ldap-attributes, posix-groups, goun-groups, posix-groups-bushy, posix-groups-bushy-dnsearch
#
# This test takes verifies the correct provisioning happens when groups are marked
# after memberships are manipulated
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
test_start "$ME" "Pspng ($flavor): provisioning groups and membership changes when marked"

################
## CONFIGURE GROUPER
## This will populate and define GROUPER_CONFIG_DIR

create_grouper_daemon_config

################
## START DOCKER

start_docker "${ME}_$flavor"


wait_for_grouper_daemon_to_be_running

create_test_folder

create_group1_and_group2

add_members_to_group1 banderson agasper bbrown705
add_group1_to_group2
await_changelog_catchup

validate_deprovisioning "$GROUP1_NAME"
validate_deprovisioning "$GROUP2_NAME"

mark_test_folder_for_provisioning

await_changelog_catchup
test_step "Waiting for changelog-triggered full sync to be completed"
run_in_grouper_daemon await-latest-full-sync-to-finish

validate_provisioning "$GROUP1_NAME" "agasper,banderson,bbrown705"
validate_provisioning "$GROUP2_NAME" "agasper,banderson,bbrown705"

#make sure extra groups were not provisioned
validate_deprovisioning "$UNPROVISIONED_GROUP_NAME"
wrap_up
assert_empty "$ERRORS" "Check for exceptions in grouper_error.log"
test_success
