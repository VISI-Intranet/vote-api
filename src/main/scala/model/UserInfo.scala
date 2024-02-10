package model

import enumClass.UserCategory.UserCategory
import enumClass.UserCategory
import enumClass.UserStatus
import enumClass.UserStatus.UserStatus


case object UserInfo {
  val category: UserCategory = UserCategory.admin
  val status: UserStatus = UserStatus.active
}
