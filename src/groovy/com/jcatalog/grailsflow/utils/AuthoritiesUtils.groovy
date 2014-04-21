/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jcatalog.grailsflow.utils;

import org.apache.commons.lang.StringUtils

/*
 * Helper methods for working with authorities
 *
 */

class AuthoritiesUtils {
  static USER_PREFIX = "USER_"
  static ROLE_PREFIX = "ROLE_"
  static GROUP_PREFIX = "GROUP_"

  /**
    * Returns user authorities using "USER_" prefix for username, "ROLE_" prefix for user roles, "GROUP_" prefix for user groups
    *
    */
  public static Collection<String> getAuthorities(Collection<String> users, Collection<String> roles, Collection<String> groups) {
    def authorities = []
    if (users) {
      authorities += getUserAuthorities(users)
    }
    if (roles) {
	    authorities += getRoleAuthorities(roles)
	  }
    if (groups) {
      authorities += getGroupAuthorities(groups)
    }
	  return authorities*.toString()
  }

  /**
    * Extracts user names from list of authorities where user items start with "USER_" prefix.
    * "USER_" prefix is skipped for returning items.
    *
    */
  public static Collection<String> getUsers(Collection<String> authorities) {
    return authorities ? authorities.findAll() { it.startsWith(USER_PREFIX) }.collect() { StringUtils.substringAfter(it, USER_PREFIX) } : []
  }

  /**
    * Create user authority from username by adding "USER_" prefix.
    *
    */
  public static String getUserAuthority(String username) {
    return username ? "${USER_PREFIX}${username}".toString() : null
  }

  /**
    * Create user authorities from user names by adding "USER_" prefix.
    *
    */
  public static Collection<String> getUserAuthorities(Collection<String> usernames) {
    return usernames ? usernames.findAll(){ it }.collect() { "${USER_PREFIX}${it}".toString() } : []
  }

  /**
    * Create role authority from role name by adding "ROLE_" prefix.
    *
    */
  public static String getRoleAuthority(String role) {
    return role ? "${ROLE_PREFIX}${role}".toString() : null
  }

  /**
    * Extracts roles names from list of authorities where roles items start "ROLE_" prefix.
    * "ROLE_" prefix is skipped for returning items.
    *
    */
  public static Collection<String> getRoles(Collection<String> authorities) {
    return authorities ? authorities.findAll() { it.startsWith(ROLE_PREFIX) }.collect() { StringUtils.substringAfter(it, ROLE_PREFIX) } : []
  }

  /**
    * Create role authorities from role names by adding "ROLE_" prefix.
    *
    */
  public static Collection<String> getRoleAuthorities(Collection<String> roles) {
    return roles ? roles.findAll(){ it }.collect() { "${ROLE_PREFIX}${it}".toString() } : []
  }

  /**
    * Create group authority from group name by adding "GROUP_" prefix.
    *
    */
  public static String getGroupAuthority(String group) {
    return group ? "${GROUP_PREFIX}${group}".toString() : null
  }

  /**
    * Extracts groups names from list of authorities where group items start with "GROUP_" prefix.
    * "GROUP_" prefix is skipped for returning items.
    *
    */
  public static Collection<String> getGroups(Collection<String> authorities) {
    return authorities ? authorities.findAll() { it.startsWith(GROUP_PREFIX) }.collect() { StringUtils.substringAfter(it, GROUP_PREFIX) } : []
  }

  /**
    * Create group authorities from group names by adding "GROUP_" prefix.
    *
    */
  public static Collection<String> getGroupAuthorities(Collection<String> groups) {
    return groups ? groups.findAll(){ it }.collect() { "${GROUP_PREFIX}${it}".toString() } : []
  }

}
