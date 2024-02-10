package model

import java.util.Date
import enumClass.Filter.Filter
import enumClass.UserCategory.UserCategory
import enumClass.Importance.Importance

import java.text.SimpleDateFormat


case class VoteModel(
                      _id: Option[String] = None,
                      authorId: Int,
                      authorCategory: UserCategory,
                      filter: Filter,
                      importance: Importance,
                      content: String,
                      titel: String,
                      date: Date,
                      time: Date,
                      canComent: String,
                      option: List[String],
                      hashtag: List[String],
                      views: List[String]
                      //views: List[ViewedModel]
               )

