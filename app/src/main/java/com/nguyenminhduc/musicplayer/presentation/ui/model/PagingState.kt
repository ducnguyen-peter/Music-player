package com.nguyenminhduc.musicplayer.presentation.ui.model

class PagingState(
    var lastPage: Int = 0,
    var nextPage: Int = 1,
    var currentPage: Int = 0,
    val pageSize: Int = 20
)