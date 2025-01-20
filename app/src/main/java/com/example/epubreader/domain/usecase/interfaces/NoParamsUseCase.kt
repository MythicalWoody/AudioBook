package com.example.epubreader.domain.usecase.interfaces

interface NoParamsUseCase<out Type> {
    suspend fun execute(): Type
}