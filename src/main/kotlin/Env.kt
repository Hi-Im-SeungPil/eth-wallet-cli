import java.io.File

fun loadEnv(): Map<String, String> {
    val map = mutableMapOf<String, String>()
    // 1. 프로젝트 폴더의 ".env" 파일을 줄 단위로 읽기
    File(".env").readLines().forEach { line ->
        if (line.isNotEmpty()) {
            val key = line.substringBefore("=")
            val value = line.substringAfter("=")
            map[key] = value
        }
    }
    // 2. 빈 줄은 건너뛰기
    // 3. 각 줄을 첫 번째 "=" 기준으로 키와 값으로 나눠 Map에 담기
    return map
}