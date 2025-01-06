package com.example.epubreader.common.exceptions

class BookNotFoundException (bookId: String) :
    Exception("Book with id $bookId not found")