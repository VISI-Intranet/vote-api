package model

import java.util.Date
import enumClass.UserCategory.UserCategory


case class CommentModel(
                    _id : Option[String] = None,
                    authorId: String,
                    authorCategory: UserCategory,
                    date: Date,
                    content: String,
                    rating: Int,
                    voteId: String,
                    views: List[String]
                  )

