package uz.rsmax.kotlin210rctest

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform