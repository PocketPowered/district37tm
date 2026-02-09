package com.district37.toastmasters.auth

/**
 * Represents a country with its dial code for phone number input
 */
data class CountryCode(
    val name: String,
    val code: String,      // ISO 3166-1 alpha-2 code (e.g., "US")
    val dialCode: String,  // E.164 country code (e.g., "+1")
    val flag: String       // Emoji flag
)

/**
 * List of country codes for phone number input
 */
object CountryCodes {
    val all: List<CountryCode> = listOf(
        CountryCode("United States", "US", "+1", "\uD83C\uDDFA\uD83C\uDDF8"),
        CountryCode("United Kingdom", "GB", "+44", "\uD83C\uDDEC\uD83C\uDDE7"),
        CountryCode("Canada", "CA", "+1", "\uD83C\uDDE8\uD83C\uDDE6"),
        CountryCode("Australia", "AU", "+61", "\uD83C\uDDE6\uD83C\uDDFA"),
        CountryCode("Germany", "DE", "+49", "\uD83C\uDDE9\uD83C\uDDEA"),
        CountryCode("France", "FR", "+33", "\uD83C\uDDEB\uD83C\uDDF7"),
        CountryCode("Japan", "JP", "+81", "\uD83C\uDDEF\uD83C\uDDF5"),
        CountryCode("China", "CN", "+86", "\uD83C\uDDE8\uD83C\uDDF3"),
        CountryCode("India", "IN", "+91", "\uD83C\uDDEE\uD83C\uDDF3"),
        CountryCode("Brazil", "BR", "+55", "\uD83C\uDDE7\uD83C\uDDF7"),
        CountryCode("Mexico", "MX", "+52", "\uD83C\uDDF2\uD83C\uDDFD"),
        CountryCode("Spain", "ES", "+34", "\uD83C\uDDEA\uD83C\uDDF8"),
        CountryCode("Italy", "IT", "+39", "\uD83C\uDDEE\uD83C\uDDF9"),
        CountryCode("Netherlands", "NL", "+31", "\uD83C\uDDF3\uD83C\uDDF1"),
        CountryCode("South Korea", "KR", "+82", "\uD83C\uDDF0\uD83C\uDDF7"),
        CountryCode("Russia", "RU", "+7", "\uD83C\uDDF7\uD83C\uDDFA"),
        CountryCode("Indonesia", "ID", "+62", "\uD83C\uDDEE\uD83C\uDDE9"),
        CountryCode("Turkey", "TR", "+90", "\uD83C\uDDF9\uD83C\uDDF7"),
        CountryCode("Saudi Arabia", "SA", "+966", "\uD83C\uDDF8\uD83C\uDDE6"),
        CountryCode("South Africa", "ZA", "+27", "\uD83C\uDDFF\uD83C\uDDE6"),
        CountryCode("Argentina", "AR", "+54", "\uD83C\uDDE6\uD83C\uDDF7"),
        CountryCode("Poland", "PL", "+48", "\uD83C\uDDF5\uD83C\uDDF1"),
        CountryCode("Thailand", "TH", "+66", "\uD83C\uDDF9\uD83C\uDDED"),
        CountryCode("Sweden", "SE", "+46", "\uD83C\uDDF8\uD83C\uDDEA"),
        CountryCode("Belgium", "BE", "+32", "\uD83C\uDDE7\uD83C\uDDEA"),
        CountryCode("Switzerland", "CH", "+41", "\uD83C\uDDE8\uD83C\uDDED"),
        CountryCode("Austria", "AT", "+43", "\uD83C\uDDE6\uD83C\uDDF9"),
        CountryCode("Norway", "NO", "+47", "\uD83C\uDDF3\uD83C\uDDF4"),
        CountryCode("Denmark", "DK", "+45", "\uD83C\uDDE9\uD83C\uDDF0"),
        CountryCode("Finland", "FI", "+358", "\uD83C\uDDEB\uD83C\uDDEE"),
        CountryCode("Ireland", "IE", "+353", "\uD83C\uDDEE\uD83C\uDDEA"),
        CountryCode("Portugal", "PT", "+351", "\uD83C\uDDF5\uD83C\uDDF9"),
        CountryCode("Greece", "GR", "+30", "\uD83C\uDDEC\uD83C\uDDF7"),
        CountryCode("New Zealand", "NZ", "+64", "\uD83C\uDDF3\uD83C\uDDFF"),
        CountryCode("Singapore", "SG", "+65", "\uD83C\uDDF8\uD83C\uDDEC"),
        CountryCode("Hong Kong", "HK", "+852", "\uD83C\uDDED\uD83C\uDDF0"),
        CountryCode("Taiwan", "TW", "+886", "\uD83C\uDDF9\uD83C\uDDFC"),
        CountryCode("Malaysia", "MY", "+60", "\uD83C\uDDF2\uD83C\uDDFE"),
        CountryCode("Philippines", "PH", "+63", "\uD83C\uDDF5\uD83C\uDDED"),
        CountryCode("Vietnam", "VN", "+84", "\uD83C\uDDFB\uD83C\uDDF3"),
        CountryCode("Israel", "IL", "+972", "\uD83C\uDDEE\uD83C\uDDF1"),
        CountryCode("United Arab Emirates", "AE", "+971", "\uD83C\uDDE6\uD83C\uDDEA"),
        CountryCode("Egypt", "EG", "+20", "\uD83C\uDDEA\uD83C\uDDEC"),
        CountryCode("Nigeria", "NG", "+234", "\uD83C\uDDF3\uD83C\uDDEC"),
        CountryCode("Kenya", "KE", "+254", "\uD83C\uDDF0\uD83C\uDDEA"),
        CountryCode("Colombia", "CO", "+57", "\uD83C\uDDE8\uD83C\uDDF4"),
        CountryCode("Chile", "CL", "+56", "\uD83C\uDDE8\uD83C\uDDF1"),
        CountryCode("Peru", "PE", "+51", "\uD83C\uDDF5\uD83C\uDDEA"),
        CountryCode("Czech Republic", "CZ", "+420", "\uD83C\uDDE8\uD83C\uDDFF"),
        CountryCode("Romania", "RO", "+40", "\uD83C\uDDF7\uD83C\uDDF4")
    ).sortedBy { it.name }

    val default: CountryCode = all.first { it.code == "US" }

    /**
     * Find country by ISO code
     */
    fun findByCode(code: String): CountryCode? = all.find { it.code == code }

    /**
     * Find country by dial code
     */
    fun findByDialCode(dialCode: String): CountryCode? = all.find { it.dialCode == dialCode }

    /**
     * Search countries by name or dial code
     */
    fun search(query: String): List<CountryCode> {
        if (query.isBlank()) return all
        val lowerQuery = query.lowercase()
        return all.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.dialCode.contains(query) ||
            it.code.lowercase().contains(lowerQuery)
        }
    }
}
