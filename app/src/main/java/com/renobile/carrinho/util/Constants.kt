package com.renobile.carrinho.util

const val PARAM_ITEM_ID = "ParamItemId"
const val PARAM_SHOW_BACK = "ParamShowBack"
const val PARAM_TYPE = "ParamType"

const val FORMAT_DATETIME_API = "yyyy-MM-dd HH:mm:ss"

const val PARAM_CART_ID = "CartId"
const val PARAM_LIST_ID = "ListId"
const val PARAM_SEARCH_TERMS = "SearchTerms"

const val ONE_SECOND: Long = 1000
const val ONE_MINUTE: Long = ONE_SECOND * 60
const val ONE_HOUR: Long = ONE_MINUTE * 60
const val ONE_DAY: Long = ONE_HOUR * 24
const val FIVE_DAYS: Long = ONE_DAY * 5

const val PREF_SORT_ORDER = "PrefSortOrder"

enum class ProductSortOrder {
    NEWEST,
    OLDEST,
    NAME_ASC,
    NAME_DESC,
    PRICE_ASC,
    PRICE_DESC
}
