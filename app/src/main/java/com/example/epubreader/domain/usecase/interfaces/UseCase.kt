package com.example.epubreader.domain.usecase.interfaces

interface UseCase<in Params, out Type> {
    suspend fun execute(params: Params): Type
}