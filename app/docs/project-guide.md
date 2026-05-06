# 프로젝트 가이드
## 개요
이 프로젝트는 Java 21 과 Gradle 을 사용하는 AI 비서 애플리케이션이다.
패키지 구조는 com.acme.assistant 아래에 llm, tool, rag 패키지로 구성된다.

## 빌드 방법
프로젝트를 빌드하려면 다음 명령을 실행한다.
./gradlew build
테스트만 실행하려면 다음 명령을 사용한다.
./gradlew test

## 환경 변수
- OPENAI_API_KEY: OpenAI API 키
- LLM_PROVIDER: LLM 제공자 (openai, anthropic, gemini, ollama)
  docs/api-reference.md
