curl -X POST http://localhost:8080/posts \
  -H "Content-Type: application/json" \
  -d '{"id":"4ba028b1-ed9a-41c1-99e7-45a511093709", "title": "PEE world", "content": "This is my first post"}' |
  jq .
