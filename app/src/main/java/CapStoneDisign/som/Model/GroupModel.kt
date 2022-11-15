package CapStoneDisign.som.Model

data class GroupModel(
    val firstUserID: String,
    val secondUserID: String?,
    val startDate: Double?,
    val year: Int?,
    val month: Int?,
    val dayOfMonth: Int?
){
    constructor():this("","",0.0,null,null,null)
}
