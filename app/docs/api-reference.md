# API 레퍼런스
## LlmClient
LLM 과 대화하기 위한 프로바이더 중립 인터페이스이다.
chat(model, messages) 메서드로 대화를 수행한다.

## EmbeddingClient
텍스트를 벡터로 변환하는 인터페이스이다.
embed(model, texts) 메서드로 임베딩을 생성한다.
text-embedding-3-small 모델은 1536 차원 벡터를 반환한다.

## ToolRegistry
도구를 등록하고 조회하는 레지스트리이다.
register(tool) 메서드로 도구를 등록하고,
getTool(name) 메서드로 이름으로 조회한다.
