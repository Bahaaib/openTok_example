package stllpt.com.bahaa

import android.webkit.URLUtil

object OpenTokConfig {
    // *** Fill the following variables using your own Project info from the OpenTok dashboard  ***
    // ***                      https://dashboard.tokbox.com/projects                           ***

    // Replace with your OpenTok API key
    val API_KEY = "45828062"
    // Replace with a generated Session ID
    val SESSION_ID = "2_MX40NTgyODA2Mn5-MTU4OTM2Njg1MzYyMn5yK2Z2T21ydjlwdHQrTlRLZGhyUGFFZzF-UH4"
    // Replace with a generated token (from the dashboard or using an OpenTok server SDK)
    val TOKEN = "T1==cGFydG5lcl9pZD00NTgyODA2MiZzaWc9OGY2NWQzZjYzYjljYjkwNTExNmI2ZTFjMTg4ODZiMTg0ZGY5Y2VlMzpzZXNzaW9uX2lkPTJfTVg0ME5UZ3lPREEyTW41LU1UVTRPVE0yTmpnMU16WXlNbjV5SzJaMlQyMXlkamx3ZEhRclRsUkxaR2h5VUdGRlp6Ri1VSDQmY3JlYXRlX3RpbWU9MTU4OTM2Njg3MSZub25jZT0wLjQxODk0NDUzODkxNjY5ODYmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTU4OTQ1MzI3MQ=="

    /*                           ***** OPTIONAL *****
     If you have set up a server to provide session information replace the null value
     in CHAT_SERVER_URL with it.

     For example: "https://yoursubdomain.com"
    */
    val CHAT_SERVER_URL: String? = null
    val SESSION_INFO_ENDPOINT = CHAT_SERVER_URL ?:"" + "/session"


    // *** The code below is to validate this configuration file. You do not need to modify it  ***

    lateinit var webServerConfigErrorMessage: String
    lateinit var hardCodedConfigErrorMessage: String

    val isWebServerConfigUrlValid: Boolean
        get() {
            if (CHAT_SERVER_URL == null || CHAT_SERVER_URL.isEmpty()) {
                webServerConfigErrorMessage = "CHAT_SERVER_URL in OpenTokConfig.java must not be null or empty"
                return false
            } else if (!(URLUtil.isHttpsUrl(CHAT_SERVER_URL) || URLUtil.isHttpUrl(CHAT_SERVER_URL))) {
                webServerConfigErrorMessage = "CHAT_SERVER_URL in OpenTokConfig.java must be specified as either http or https"
                return false
            } else if (!URLUtil.isValidUrl(CHAT_SERVER_URL)) {
                webServerConfigErrorMessage = "CHAT_SERVER_URL in OpenTokConfig.java is not a valid URL"
                return false
            } else {
                return true
            }
        }

    fun areHardCodedConfigsValid(): Boolean {
        if (API_KEY != null && !API_KEY.isEmpty()
                && SESSION_ID != null && !SESSION_ID.isEmpty()
                && TOKEN != null && !TOKEN.isEmpty()) {
            return true
        } else {
            hardCodedConfigErrorMessage = "API KEY, SESSION ID and TOKEN in OpenTokConfig.java cannot be null or empty."
            return false
        }
    }
}
