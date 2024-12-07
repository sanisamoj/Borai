package com.sanisamoj.services.user

import com.sanisamoj.data.models.dataclass.Address
import com.sanisamoj.data.models.dataclass.Doc
import com.sanisamoj.data.models.dataclass.UserCreateRequest
import com.sanisamoj.data.models.dataclass.UserPreference
import com.sanisamoj.data.models.enums.AccountType
import java.util.UUID

object UserRequestFactory {

    const val PASSWORD_TEST: String = "securePassword123"

    private fun generateRandomPhone(): String {
        val countryCode = "+55"
        val randomNumber = (1000000000..9999999999).random()
        return "$countryCode$randomNumber"
    }

    private fun generateRandomEmail(): String {
        return "user${UUID.randomUUID()}@example.com"
    }

    private fun generateRandomNick(): String {
        return "user${UUID.randomUUID().toString().take(8)}"
    }

    private fun generateRandomCity(): String {
        val cities = listOf("São Paulo", "Rio de Janeiro", "Belo Horizonte", "Curitiba", "Salvador")
        return cities.random()
    }

    private fun generateRandomDoc(): Doc {
        val docTypes = listOf("passport", "id_card", "driver_license")
        val docType = docTypes.random()
        val docNumber = UUID.randomUUID().toString().take(10)
        return Doc(type = docType, number = docNumber)
    }

    fun validUserCreateRequest(): UserCreateRequest {
        return UserCreateRequest(
            nick = generateRandomNick(),
            bio = "I love coding and testing",
            username = generateRandomNick(),  // Username também será aleatório
            imageProfile = null,
            email = generateRandomEmail(),
            password = PASSWORD_TEST,
            phone = generateRandomPhone(),
            type = AccountType.PARTICIPANT.name,
            preferences = UserPreference(eventPreferences = listOf("Technology")),
            doc = generateRandomDoc(),
            address = Address(
                city = generateRandomCity(),
                uf = "SP"
            )
        )
    }

}