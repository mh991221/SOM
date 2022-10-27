package CapStoneDisign.som.Model

data class UserModel(
    val name: String,
    val email: String,
    val password: String,
    val phoneNumber: String,
    val groupID: String?
){
     constructor(): this("","","","",null)
}
