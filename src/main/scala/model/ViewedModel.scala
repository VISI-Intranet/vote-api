package model

import enumClass.UserCategory.UserCategory

case class ViewedModel(
                        _id: Option[String] = None,
                        userId: String,
                        userCategory: UserCategory
                      )

