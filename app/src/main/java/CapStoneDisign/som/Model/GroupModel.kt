package CapStoneDisign.som.Model

data class GroupModel(
    val firstUserID: String,
    val secondUserID: String?
){
    constructor():this("","")
}
